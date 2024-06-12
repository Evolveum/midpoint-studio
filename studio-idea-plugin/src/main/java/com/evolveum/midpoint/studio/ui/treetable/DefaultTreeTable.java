package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeCellRenderer;

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
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            ColumnInfo ci = getTableModel().getColumnInfo(i);
            TableColumn column = this.columnModel.getColumn(i);

            TableCellEditor editor = ci.getEditor(null);
            if (editor != null) {
                column.setCellEditor(editor);
            }

            TableCellRenderer renderer = ci.getRenderer(null);
            if (renderer != null) {
                column.setCellRenderer(renderer);
            }

            if (ci instanceof DefaultColumnInfo<?, ?> dci) {
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

        TreeCellRenderer treeCellRenderer = createTreeCellRenderer();
        if (treeCellRenderer != null) {
            setTreeCellRenderer(treeCellRenderer);
        }
    }

    protected TreeCellRenderer createTreeCellRenderer() {
        Integer treeColumnIndex = null;
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            ColumnInfo ci = getTableModel().getColumnInfo(i);

            if (treeColumnIndex == null && TreeTableModel.class.equals(ci.getColumnClass())) {
                treeColumnIndex = i;
            }
        }

        if (treeColumnIndex == null) {
            return null;
        }

        int index = treeColumnIndex;

        return new NodeRenderer() {

            @Override
            public void customizeCellRenderer(
                    @NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {

                Icon icon = customizeTreeCellIcon(value);
                if (icon != null) {
                    setIcon(icon);
                }

                value = getTableModel().getColumnInfo(index).valueOf(value);

                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        };
    }

    protected Icon customizeTreeCellIcon(Object value) {
        return null;
    }
}
