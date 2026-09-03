package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
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

        Map<Integer, String> columnFilters = new HashMap<>();

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

            if (ci instanceof FilterableColumnInfo<?, ?> filterableColumnInfo
                    && filterableColumnInfo.hasFunnelFilter()
            ) {
                column.setHeaderRenderer(new FilterHeaderRenderer(false));
            }
        }

        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = tableHeader.columnAtPoint(e.getPoint());
                if (columnIndex < 0) return;
                var headerRenderer = tableHeader.getColumnModel().getColumn(columnIndex).getHeaderRenderer();

                if (headerRenderer instanceof FilterHeaderRenderer filterHeaderRenderer
                        && isClickOnFunnel(tableHeader, columnIndex, e.getX())
                ) {
                    showFilterPopup(columnFilters, tableHeader, filterHeaderRenderer, columnIndex, e);
                }
            }
        });

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

                value = getTableModel().getValueAt(value, index);

                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        };
    }

    protected Icon customizeTreeCellIcon(Object value) {
        return null;
    }

    private boolean isClickOnFunnel(JTableHeader header, int viewColumn, int mouseX) {
        Rectangle rect = header.getHeaderRect(viewColumn);
        int iconWidth = 16;
        int padding = 6;
        int iconStartX = rect.x + rect.width - iconWidth - padding;
        return mouseX >= iconStartX;
    }

    public void showFilterPopup(
            Map<Integer, String> columnFilters,
            JTableHeader header,
            FilterHeaderRenderer filterHeaderRenderer,
            int modelColumnIndex,
            MouseEvent e
    ) {
        SearchTextField searchField = new SearchTextField();
        searchField.setText(columnFilters.getOrDefault(modelColumnIndex, ""));
        searchField.setBorder(JBUI.Borders.empty(5));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(searchField, searchField.getTextEditor())
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .setResizable(false)
                .createPopup();

        searchField.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }

            private void update() {
                String text = searchField.getText().trim();
                columnFilters.put(modelColumnIndex, text);
                getTableModel().applyFilter(text);
            }
        });

        popup.addListener(new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                String text = searchField.getText();
                filterHeaderRenderer.setActiveFilter(text != null && !text.isEmpty());
                header.revalidate();
                header.repaint();
            }
        });

        popup.show(new RelativePoint(e.getComponent(), new Point(e.getX(), e.getComponent().getHeight())));
    }

    private static class FilterHeaderRenderer implements TableCellRenderer {

        boolean activeFilter;

        public FilterHeaderRenderer(boolean activeFilter) {
            this.activeFilter = activeFilter;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            panel.setBackground(table.getTableHeader().getBackground());

            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));

            FunnelIconLabel funnel = new FunnelIconLabel(AllIcons.General.Filter);
            funnel.setActive(activeFilter);
            funnel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

            panel.add(label, BorderLayout.WEST);
            panel.add(funnel, BorderLayout.EAST);

            return panel;
        }

        public void setActiveFilter(boolean activeFilter) {
            this.activeFilter = activeFilter;
        }
    }

    private static class FunnelIconLabel extends JLabel {

        boolean active;

        public FunnelIconLabel(Icon icon) {
            super(icon);
            setOpaque(false);
        }

        public void setActive(boolean active) {
            this.active = active;
            Graphics g = getGraphics();
            if (g != null) {
                paintComponent(g);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (!active) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int dotSize = 6;
            int borderSize = 10;

            int x = getWidth() - borderSize - 5;
            int y = 1;

            g2.setColor(JBColor.namedColor("Panel.background",
                    new JBColor(0xffffff, 0x3c3f41)));
            g2.fillOval(x, y, borderSize, borderSize);

            int innerX = x + (borderSize - dotSize) / 2;
            int innerY = y + (borderSize - dotSize) / 2;

            g2.setColor(JBColor.GREEN);
            g2.fillOval(innerX, innerY, dotSize, dotSize);

            g2.dispose();
        }
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
