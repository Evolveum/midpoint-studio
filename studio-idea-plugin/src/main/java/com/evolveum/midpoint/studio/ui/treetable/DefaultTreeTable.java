package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;

public class DefaultTreeTable<T, M extends DefaultTreeTableModel<T>> extends TreeTable {

    public DefaultTreeTable(M model) {
        super(model);

        setupComponent();
    }

    @Override
    public M getTableModel() {
        return (M) super.getTableModel();
    }

    private void setupComponent() {
        // Honor per-column preferred widths; show horizontal scrollbar when columns overflow
        setAutoResizeMode(AUTO_RESIZE_OFF);

        TreeTableSpeedSearch.installOn(this, this::getSpeedSearchText);

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

    /**
     * Returns the text used for speed search matching for the given tree path.
     * Defaults to {@code toString()} on the last path component.
     * Subclasses can override to match against a specific field (e.g. a label or name).
     */
    @Nullable
    protected String getSpeedSearchText(TreePath path) {
        if (path == null) {
            return null;
        }
        return path.getLastPathComponent().toString();
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

    @Override
    public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        M model = getTableModel();

        // Resolve the user object from the tree path node
        Object userObject = null;
        TreePath path = getTree().getPathForRow(row);

        Object node = null;
        if (path != null) {
            node = path.getLastPathComponent();
            userObject = extractUserObject(node);
        }

        if (userObject == null) {
            return c;
        }

        // Row-level style from the model's RowStyler (receives the raw tree node;
        // the lambda uses instanceof to extract what it needs).
        Style rowStyle = null;
        RowStyleProvider rowStyleProvider = model.getRowStyler();
        if (rowStyleProvider != null) {
            rowStyle = rowStyleProvider.getStyle(node);
        }

        // Cell-level style from the ColumnInfo (if it is a DefaultColumnInfo).
        // Convert view column index to model column index — they diverge when columns are hidden.
        Style style = null;
        int modelColumn = convertColumnIndexToModel(column);
        ColumnInfo ci = model.getColumnInfo(modelColumn);
        if (ci instanceof DefaultColumnInfo<?, ?> dci) {
            style = dci.getStyleUnchecked(userObject);
        }

        // Merge: cell overrides row
        Style effectiveStyle = style != null ? style.mergeOver(rowStyle) : rowStyle;

        boolean selected = isRowSelected(row);

        // Always explicitly set foreground/background for non-selected cells.
        // Falling back to the table default is required to prevent renderer-reuse bleed:
        // a shared DefaultTableCellRenderer set to green for STATUS would remain green
        // when reused for the next column unless we reset it here.
        if (!selected) {
            c.setForeground(effectiveStyle != null && effectiveStyle.getForeground() != null
                    ? effectiveStyle.getForeground()
                    : getForeground());
            c.setBackground(effectiveStyle != null && effectiveStyle.getBackground() != null
                    ? effectiveStyle.getBackground()
                    : getBackground());
        }
        if (effectiveStyle != null && effectiveStyle.getFont() != null) {
            c.setFont(effectiveStyle.getFont());
        }

        return c;
    }

    /**
     * Extracts the user object from a tree node.
     * Supports both {@link UserObjectNode} (custom studio nodes) and
     * {@link DefaultMutableTreeNode} (standard Swing nodes).
     */
    private static Object extractUserObject(Object node) {
        if (node instanceof UserObjectNode uon) {
            return uon.getUserObject();
        }
        if (node instanceof DefaultMutableTreeNode dmtn) {
            return dmtn.getUserObject();
        }
        return null;
    }
}
