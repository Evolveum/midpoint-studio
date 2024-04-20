package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.DoubleClickListener;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

public class SynchronizationTree extends CheckboxTree {

    private final Project project;

    public SynchronizationTree(@NotNull Project project, @NotNull SynchronizationTreeModel model) {
        super(new TreeRenderer(), null, new CheckPolicy(true, true, false, true));

        setModel(model);

        this.project = project;

        setup();
    }

    private void setup() {
        setRootVisible(false);

//        setCellRenderer(new LabelBasedRenderer.Tree() {
//
//            @Override
//            public @NotNull Component getTreeCellRendererComponent(
//                    @NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf,
//                    int row, boolean focused) {
//
//                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);
//
//                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//                Color color = computeColor(node.getUserObject());
//                if (color != null) {
//                    c.setForeground(color);
//                }
//
//                return c;
//            }
//        });

        DoubleClickListener doubleClickListener = new DoubleClickListener() {

            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent event) {
                TreePath path = TreeUtil.getPathForLocation(SynchronizationTree.this, event.getX(), event.getY());
                if (path == null) {
                    return false;
                }

                // todo if checkbox is there: if (tree.getPathIfCheckBoxClicked(e.point) != null) return false

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node == null) {
                    return false;
                }

                doubleClickPerformed(node);

                return true;
            }
        };
        doubleClickListener.installOn(this);
    }

    private void doubleClickPerformed(DefaultMutableTreeNode node) {
        Object object = node.getUserObject();
        if (object instanceof SynchronizationFileItem file) {
            if (file.objects().size() == 1) {
                openSynchronizationEditor(file.objects().get(0));
            }
        } else if (object instanceof SynchronizationObjectItem obj) {
            openSynchronizationEditor(obj);
        }
    }

    private void openSynchronizationEditor(SynchronizationObjectItem object) {
        MidPointObject leftObject = object.local();
        MidPointObject rightObject = object.remote();

        LightVirtualFile leftFile = new LightVirtualFile(leftObject.getName(), leftObject.getContent());
        LightVirtualFile rightFile = new LightVirtualFile(rightObject.getName(), rightObject.getContent());

        DiffSource left = new DiffSource(leftFile.getName(), leftFile, DiffSourceType.LOCAL);
        DiffSource right = new DiffSource(rightFile.getName(), rightFile, DiffSourceType.REMOTE);

        DiffProcessor processor = new DiffProcessor(project, left, right);
        processor.initialize();
        DiffVirtualFile file = new DiffVirtualFile(processor);

        MidPointUtils.openFile(project, file);
    }

    private Color computeColor(Object userObject) {
        // todo implement

        return null;
    }

    @Override
    public String convertValueToText(
            Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof SynchronizationFileItem file) {
            value = file.local().getName();
        } else if (node.getUserObject() instanceof SynchronizationObjectItem object) {
            value = object.name();
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

    private static class TreeRenderer extends CheckboxTreeCellRenderer {

        @Override
        public void customizeRenderer(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // todo implement

            String text = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);

            getTextRenderer().append(text);
        }
    }
}
