package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
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
        SynchronizationObjectItem item = null;
        if (object instanceof SynchronizationFileItem<?> file) {
            if (file.getObjects().size() == 1) {
                item = file.getObjects().get(0);
            }
        } else if (object instanceof SynchronizationObjectItem obj) {
            item = obj;
        }

        if (item != null) {
            SynchronizationManager.get(project).getSession().openSynchronizationEditor(item);
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

            if (si.isNew()) {
                return SynchronizationUtil.getColorForModificationType(ModificationType.ADD);
            }

            if (si.isUnchanged()) {
                return SynchronizationUtil.getColor(SynchronizationUtil.IGNORED);
            }

            ModificationType modification = si.hasLocalChanges() || si.hasRemoteChanges() ?
                    ModificationType.REPLACE : null;

            return SynchronizationUtil.getColorForModificationType(modification);
        }
    }
}
