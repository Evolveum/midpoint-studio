package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class SynchronizationTree extends Tree {

    public SynchronizationTree(@NotNull SynchronizationTreeModel model) {
        super(model);

        setup();
    }

    private void setup() {
        setRootVisible(false);

        setCellRenderer(new LabelBasedRenderer.Tree() {

            @Override
            public @NotNull Component getTreeCellRendererComponent(
                    @NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf,
                    int row, boolean focused) {

                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Color color = computeColor(node.getUserObject());
                if (color != null) {
                    c.setForeground(color);
                }

                return c;
            }
        });
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

    @Override
    public SynchronizationTreeModel getModel() {
        return (SynchronizationTreeModel) super.getModel();
    }
}
