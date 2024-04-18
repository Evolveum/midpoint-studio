package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ui.JBColor;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ObjectDeltaTree<O extends ObjectType> extends Tree {

    public ObjectDeltaTree(@NotNull ObjectDeltaTreeModel<O> model) {
        super(model);  // todo implement

        setup();
    }

    private void setup() {
        setCellRenderer(new LabelBasedRenderer.Tree() {

            @Override
            public @NotNull Component getTreeCellRendererComponent(@NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (!(node.getUserObject() instanceof DeltaItem di)) {
                    return c;
                }

                switch (di.modificationType()) {
                    case ADD:
                        c.setForeground(JBColor.GREEN);
                        break;
                    case DELETE:
                        c.setForeground(JBColor.RED);
                        break;
                    case REPLACE:
                        c.setForeground(JBColor.BLUE);
                        break;
                }

                return c;
            }
        });
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof DeltaItem di) {
            value = di.value();
        } else if (node.getUserObject() instanceof ItemDelta<?,?> id) {
            value = id.getPath().toString();
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}
