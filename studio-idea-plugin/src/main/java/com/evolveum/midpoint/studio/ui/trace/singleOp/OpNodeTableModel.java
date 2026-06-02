package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.treetable.Style;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * Two-column tree table model for {@link AbstractOpTreePanel}.
 * Columns: Item (tree) and Variable (value). Background color is driven by
 * {@link Node#getBackgroundColor()} via the model's RowStyler.
 */
public class OpNodeTableModel extends DefaultTreeTableModel<Node<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public OpNodeTableModel() {
        super((List<ColumnInfo>) (List<?>) List.of(
                new DefaultColumnInfo<Node<?>, Object>("Item", TreeTableModel.class, null).preferredWidth(150),
                new DefaultColumnInfo<Node<?>, String>("Variable", String.class, null).preferredWidth(400)
        ));

        setRowStyler(treeNode -> {
            if (!(treeNode instanceof Node<?> n) || n.getBackgroundColor() == null) {
                return null;
            }
            return Style.background(n.getBackgroundColor());
        });
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (!(node instanceof Node<?> n)) {
            return null;
        }
        return switch (column) {
            case 0 -> n.getLabel();
            case 1 -> n.getValue();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void setRoot(DefaultMutableTreeNode newRoot) {
        super.setRoot(newRoot);
    }
}
