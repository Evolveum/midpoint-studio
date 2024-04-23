package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.LightVirtualFile;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        if (object instanceof SyncFileItem file) {
            if (file.getObjects().size() == 1) {
                openSynchronizationEditor(file.getObjects().get(0));
            }
        } else if (object instanceof SyncObjecItem obj) {
            openSynchronizationEditor(obj);
        }
    }

    private void openSynchronizationEditor(SyncObjecItem object) {
        MidPointObject leftObject = object.getItem().local();
        MidPointObject rightObject = object.getItem().remote();

        LightVirtualFile leftFile = new LightVirtualFile(leftObject.getName() + ".xml", leftObject.getContent());
        LightVirtualFile rightFile = new LightVirtualFile(rightObject.getName() + ".xml", rightObject.getContent());

        DiffSource left = new DiffSource(leftFile.getName(), leftFile, DiffSourceType.LOCAL);
        DiffSource right = new DiffSource(rightFile.getName(), rightFile, DiffSourceType.REMOTE);

        DiffProcessor<? extends ObjectType> processor = new DiffProcessor<>(project, left, right) {

            @Override
            protected void acceptPerformed() {
                super.acceptPerformed();

                updateSynchronizationState(this, object);
            }
        };
        processor.initialize();
        DiffVirtualFile file = new DiffVirtualFile(processor);

        MidPointUtils.openFile(project, file);
    }

    private void updateSynchronizationState(DiffProcessor<?> processor, SyncObjecItem object) {
        try {
            PrismObject<? extends ObjectType> result = processor.getLeftObject();
            // todo implement, this is bad
            PrismObject<? extends ObjectType> leftInitial =
                    ClientUtils.createParser(
                            MidPointUtils.DEFAULT_PRISM_CONTEXT, object.getLocalObject().getContent()).parse();

            if (!result.equivalent(leftInitial)) {
                object.setModificationType(ModificationType.REPLACE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String convertValueToText(
            Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        if (!(value instanceof DefaultMutableTreeNode node)) {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }

        value = ((SynchronizationTreeModel) getModel()).convertValueToText(node.getUserObject());

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

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

            getTextRenderer().append(text);
        }

        private Color computeColor(Object userObject) {
            ModificationType modification = null;
            if (userObject instanceof SyncObjecItem object) {
                modification = object.getModificationType();
            } else if (userObject instanceof SyncFileItem file) {
                Set<ModificationType> set = file.getObjects().stream()
                        .map(o -> o.getModificationType())
                        .collect(Collectors.toSet());
                modification = SynchronizationUtil.getModificationType(set);
            }

            return SynchronizationUtil.getColorForModificationType(modification);
        }
    }
}
