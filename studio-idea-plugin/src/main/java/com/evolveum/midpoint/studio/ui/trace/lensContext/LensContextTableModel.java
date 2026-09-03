package com.evolveum.midpoint.studio.ui.trace.lensContext;

import com.evolveum.midpoint.studio.ui.trace.lens.PrismNode;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * Tree table model for the lens context panel. Columns: Item (tree), Old, Current, New.
 */
public class LensContextTableModel extends DefaultTreeTableModel<PrismNode> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LensContextTableModel() {
        super((List<ColumnInfo>) (List<?>) List.of(
                new DefaultColumnInfo<PrismNode, Object>("Item", TreeTableModel.class, null).preferredWidth(200),
                new DefaultColumnInfo<PrismNode, String>("Old", String.class, null).preferredWidth(100),
                new DefaultColumnInfo<PrismNode, String>("Current", String.class, null).preferredWidth(100),
                new DefaultColumnInfo<PrismNode, String>("New", String.class, null).preferredWidth(100)
        ));
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (!(node instanceof PrismNode pn)) {
            return null;
        }
        return switch (column) {
            case 0 -> pn.getLabel();
            case 1 -> safeValue(pn, 0);
            case 2 -> safeValue(pn, 1);
            case 3 -> safeValue(pn, 2);
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void setRoots(List<PrismNode> roots) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        roots.forEach(root::add);
        setRoot(root);
    }

    private static String safeValue(PrismNode node, int index) {
        try {
            return node.getValue(index);
        } catch (Exception e) {
            return "";
        }
    }
}
