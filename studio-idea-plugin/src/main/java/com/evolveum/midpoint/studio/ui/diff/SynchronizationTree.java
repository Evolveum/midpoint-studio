package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.ui.synchronization.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

public class SynchronizationTree extends CheckboxTree implements Disposable {

    private final Project project;

    public SynchronizationTree(@NotNull Project project, @NotNull SynchronizationTreeModel model) {
        super(new TreeRenderer(), null, new CheckPolicy(true, true, false, true));

        setModel(model);

        this.project = project;

        setup();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void setup() {
        setRootVisible(false);

        DoubleClickListener doubleClickListener = new DoubleClickListener() {

            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent event) {
                TreePath path = getClosestPathForLocation(event.getX(), event.getY());
                if (path == null) {
                    return false;
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node == null) {
                    return false;
                }

                doubleClickPerformed(node);

                return true;
            }
        };
        doubleClickListener.installOn(this);
        Disposer.register(this, () -> doubleClickListener.uninstall(this));
    }

    @Override
    protected void onDoubleClick(CheckedTreeNode node) {
        doubleClickPerformed(node);
    }

    private void doubleClickPerformed(DefaultMutableTreeNode node) {
        Object object = node.getUserObject();
        if (object instanceof SynchronizationFileItem<?> file) {
            if (file.getObjects().size() == 1) {
                openSynchronizationEditor(file.getObjects().get(0));
            }
        } else if (object instanceof SynchronizationObjectItem obj) {
            openSynchronizationEditor(obj);
        }
    }

    private <O extends ObjectType> void openSynchronizationEditor(SynchronizationObjectItem object) {
        MidPointObject rightObject = object.getRemote();

        VirtualFile leftRealFile = object.getFileItem().getFile();

        DiffSource<O> left = new DiffSource(
                leftRealFile.getName(), DiffSourceType.LOCAL, object.getLocalObject().getCurrent());
        DiffSource<O> right = new DiffSource(
                rightObject.getName() + ".xml", DiffSourceType.REMOTE, object.getRemoteObject().getCurrent());

        DiffProcessor<? extends ObjectType> processor = new DiffProcessor<>(project, left, right) {

            @Override
            protected void acceptPerformed() {
                super.acceptPerformed();

                updateSynchronizationState(this, object, getDirection());
            }
        };
        processor.initialize();
        DiffVirtualFile file = new DiffVirtualFile(processor);

        MidPointUtils.openFile(project, file);
    }

    private <O extends ObjectType> void updateSynchronizationState(
            DiffProcessor<O> processor, SynchronizationObjectItem object, DiffProcessor.Direction direction) {
        try {
            PrismObjectHolder statefulPrismObject =
                    direction == DiffProcessor.Direction.LEFT_TO_RIGHT ? object.getRemoteObject() : object.getLocalObject();

            PrismObject<O> prismObject = processor.getTargetObject();

            statefulPrismObject.setCurrent(prismObject.clone());

            if (processor.hasChanges()) {
                getTreeModel().nodesChanged(new Object[]{object});
            } else {
                // todo nodes should be removed - there's nothing we can do with them
                //  figure out parent (file) if needed and hide, same check should happen after save local/remote
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private SynchronizationTreeModel getTreeModel() {
        return (SynchronizationTreeModel) super.getModel();
    }

    @Override
    public String convertValueToText(
            Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        if (!(value instanceof DefaultMutableTreeNode node)) {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }

        value = getTreeModel().convertValueToText(node.getUserObject());

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

    /**
     * @param <T>
     * @return Array of user objects from checked nodes
     */
    @Override
    public <T> T[] getCheckedNodes(Class<? extends T> nodeType, @Nullable Tree.NodeFilter<? super T> filter) {
        if (getModel().getRoot() == null) {
            return (T[]) new Object[0];
        }

        return super.getCheckedNodes(nodeType, filter);
    }

    private static class TreeRenderer extends CheckboxTreeCellRenderer {

        @Override
        public void customizeRenderer(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (!(value instanceof DefaultMutableTreeNode node)) {
                return;
            }

            String text = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);

            Color foreground = computeColor(node.getUserObject());
            if (foreground != null) {
                getTextRenderer().setForeground(foreground);
            }

            Icon icon = computeIcon(node.getUserObject());
            if (icon != null) {
                getTextRenderer().setIcon(icon);
            }

            getTextRenderer().append(text);
        }

        private Icon computeIcon(Object userObject) {
            if (!(userObject instanceof SynchronizationItem si)) {
                return null;
            }

            return si.getType() == SynchronizationItemType.FILE ? AllIcons.FileTypes.Xml : MidPointIcons.Midpoint;
        }

        private Color computeColor(Object userObject) {
            if (!(userObject instanceof SynchronizationItem si)) {
                return null;
            }

            ModificationType modification = si.hasLocalChanges() || si.hasRemoteChanges() ?
                    ModificationType.REPLACE : null;

            return SynchronizationUtil.getColorForModificationType(modification);
        }
    }
}
