package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;

public class DefaultTreeTable<M extends DefaultTreeTableModel> extends TreeTable {

    public DefaultTreeTable(M model) {
        super(model);

        setupComponent();
    }

    @Override
    public M getTableModel() {
        return (M) super.getTableModel();
    }

    private void setupComponent() {
        Integer treeColumnIndex = null;
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            ColumnInfo ci = getTableModel().getColumnInfo(i);

            if (treeColumnIndex == null && TreeTableModel.class.equals(ci.getColumnClass())) {
                treeColumnIndex = i;
            }

            if (ci instanceof DefaultColumnInfo<?, ?> dci) {
                TableColumn column = this.columnModel.getColumn(i);
                if (dci.getMinWidth() != null) {
                    column.setMinWidth(dci.getMinWidth());
                }
                if (dci.getMaxWidth() != null) {
                    column.setMaxWidth(dci.getMaxWidth());
                }
                if (dci.getPreferredWidth() != null) {
                    column.setPreferredWidth(dci.getPreferredWidth());
                }
            }
        }

        if (treeColumnIndex != null) {
            int index = treeColumnIndex;

            setTreeCellRenderer(new NodeRenderer() {

                @Override
                public void customizeCellRenderer(
                        @NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf,
                        int row, boolean hasFocus) {

                    value = getTableModel().getColumnInfo(index).valueOf(value);

                    super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
                }
            });
        }
    }
}
