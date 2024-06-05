package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ObjectDeltaTree<O extends ObjectType> extends Tree implements Disposable {

    private static final Logger LOG = Logger.getInstance(ObjectDeltaTree.class);

    public ObjectDeltaTree(@NotNull ObjectDeltaTreeModel<O> model) {
        super(model);

        setup();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void setup() {
        setRootVisible(false);

        setCellRenderer(new LabelBasedRenderer.Tree() {

            @Override
            public @NotNull Component getTreeCellRendererComponent(
                    @NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf,
                    int row, boolean focused) {

                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);

                if (!(value instanceof DefaultMutableTreeNode node)) {
                    return c;
                }

                Color color = computeColor(node.getUserObject());
                if (color != null) {
                    c.setForeground(color);
                }

                return c;
            }
        });
    }

    private Color computeColor(Object userObject) {
        if (!(userObject instanceof ObjectDeltaTreeNode<?> odt)) {
            return null;
        }

        ModificationType type = odt.getModificationType();

        return SynchronizationUtil.getColorForModificationType(type);
    }

    private ModificationType getModificationType(Object userObject) {
        ModificationType type = null;
        if (userObject instanceof ItemDeltaValueTreeNode idvtn) {
            type = idvtn.getModificationType();
        } else if (userObject instanceof ItemDeltaTreeNode idtn) {
            type = getModificationType(idtn.getValue());
        } else if (userObject instanceof ObjectDelta<?> od) {
            type = getModificationType(od);
        }

        return type;
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        if (node.getUserObject() instanceof ObjectDeltaTreeNode<?> odt) {
            value = odt.getText();
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}
