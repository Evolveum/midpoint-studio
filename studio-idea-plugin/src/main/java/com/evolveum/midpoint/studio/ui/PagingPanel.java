package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PagingPanel extends JPanel {

    private static final Integer[] pageSizeValues = new Integer[]{2, 10, 20, 50, 100};

    private JButton previous;
    private JButton next;
    private JComboBox<Integer> pageSize;
    private JLabel text;
    private JPanel root;

    private int pageIndex = 0;
    private Integer count = 0;

    public PagingPanel() {
        super(new BorderLayout());

        previous.addActionListener(e -> {
            pageIndex--;
            validateComponents();
        });

        next.addActionListener(e -> {
            pageIndex++;
            validateComponents();
        });

        pageSize.addItemListener(e -> {
            refreshTable();
        });
    }

    private void validateComponents() {
        previous.setEnabled(pageIndex > 0);

        int pageSize = (int) this.pageSize.getSelectedItem();
        boolean nextEnabled = false;
        if (count == null) {
            nextEnabled = true;
        } else {
            nextEnabled = (pageIndex * pageSize + pageSize) < count;
        }
        next.setEnabled(nextEnabled);
    }
    public PagingPanel showText(boolean showText) {
        text.setVisible(showText);
        return this;
    }

//    public PagingPanel pageSizeValues(Integer[] pageSizeValues) {
//        this.pageSizeValues = pageSizeValues;
//        return this;
//    }

    private void createUIComponents() {
        root = new JPanel();
        add(root, BorderLayout.CENTER);

        pageSize = new ComboBox<>();
        pageSize.setModel(new CollectionComboBoxModel<>(Arrays.asList(pageSizeValues)));

//        validateComponents();
    }

    private void updatePagingLabel() {
        int from = getPageIndex() * getPageSize();
        from++;

        int to = from + getPageSize();
        if (count != null && to > count) {
            to = count;
        }
        to--;

        StringBuilder sb = new StringBuilder();
        sb.append(from).append(" to ").append(to);
        if (count != null) {
            sb.append(" of ").append(count);
        }

        text.setText(sb.toString());
    }

    public void updateCount(Integer count) {
        this.count = count;

        updatePagingLabel();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return (int) pageSize.getSelectedItem();
    }

    public void refreshTable() {

    }
}
