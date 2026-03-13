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
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

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

            if (ci instanceof FilterableColumnInfo<?, ?> filterableColumnInfo) {
                if (filterableColumnInfo.hasFunnelFilter()) {
                    JTableHeader header = getTableHeader();
                    Map<Integer, String> columnFilters = new HashMap<>();

                    var filterHeaderRenderer = new FilterHeaderRenderer(false);

                    column.setHeaderRenderer(filterHeaderRenderer);

                    header.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int viewColumn = header.columnAtPoint(e.getPoint());
                            if (viewColumn < 0) return;

                            if (isClickOnFunnel(header, viewColumn, e.getX())) {
                                showFilterPopup(columnFilters, header, filterHeaderRenderer, viewColumn, e);
                            }
                        }
                    });
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

    private boolean isClickOnFunnel(JTableHeader header, int viewColumn, int mouseX) {
        Rectangle rect = header.getHeaderRect(viewColumn);
        int iconWidth = 16;
        int padding = 6;
        int iconStartX = rect.x + rect.width - iconWidth - padding;
        return mouseX >= iconStartX;
    }

    public void showFilterPopup(Map<Integer, String> columnFilters, JTableHeader header, FilterHeaderRenderer filterHeaderRenderer, int modelColumnIndex, MouseEvent e) {
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

        // change state of filter funnel
//        popup.addListener(new JBPopupListener() {
//            @Override
//            public void onClosed(@NotNull LightweightWindowEvent event) {
//                String text = searchField.getText();
//                filterHeaderRenderer.setActiveFilter(text != null && !text.isEmpty());
//                header.revalidate();
//                header.repaint();
//            }
//        });

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

            FunnelIconLabel funnel = new FunnelIconLabel(AllIcons.General.Filter, activeFilter);
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

        public FunnelIconLabel(Icon icon, boolean active) {
            super(icon);
            setOpaque(false);
            this.active = active;
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

            g2.setColor(JBColor.namedColor("Panel.background", new JBColor(0xffffff, 0x3c3f41)));
            g2.fillOval(x, y, borderSize, borderSize);

            int innerX = x + (borderSize - dotSize) / 2;
            int innerY = y + (borderSize - dotSize) / 2;

            g2.setColor(JBColor.GREEN);
            g2.fillOval(innerX, innerY, dotSize, dotSize);

            g2.dispose();
        }
    }
}
