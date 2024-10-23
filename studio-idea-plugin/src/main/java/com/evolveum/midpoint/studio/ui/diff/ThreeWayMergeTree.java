package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.schema.delta.ItemTreeDelta;
import com.evolveum.midpoint.schema.delta.ItemTreeDeltaValue;
import com.intellij.openapi.Disposable;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

import javax.swing.tree.DefaultMutableTreeNode;

public class ThreeWayMergeTree extends Tree implements Disposable {

    public ThreeWayMergeTree(ThreeWayMergeTreeModel model) {
        super(model);

        setup();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void setup() {
        setRootVisible(false);

        setCellRenderer(new LabelBasedRenderer.Tree());
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof DefaultMutableTreeNode node)) {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }

        Object userObject = node.getUserObject();
        if (!(userObject instanceof ThreeWayMergeNode twNode)) {
            return node.toString();
        }

        if (twNode.value() instanceof ItemTreeDelta<?,?,?,?>) {
            return "" + twNode.value();
        }

        if (twNode.value() instanceof ItemTreeDeltaValue<?,?> itdv && itdv.getModificationType() == null) {
            return "" + twNode.value();
        }

        return (twNode.direction() == ThreeWayMergeDirection.LEFT_TO_RIGHT ? ">>> " : "<<< ") + " " + twNode.value();
    }
}
