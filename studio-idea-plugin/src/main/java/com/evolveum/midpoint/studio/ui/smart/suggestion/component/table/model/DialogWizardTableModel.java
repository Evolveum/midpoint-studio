package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model;

import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.List;

public class DialogWizardTableModel<T> extends DefaultTreeTableModel<List<T>> {

    List<ColumnInfo> columns;
    List<T> data;

    public DialogWizardTableModel(List<ColumnInfo> columns) {
        super(columns);
        this.columns = columns;
    }

    private void buildTree() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();

        for (int i = 0; i < root.getChildCount(); i++) {
            root.remove(i);
        }

        for (T item : data) {
            root.add(new DefaultMutableTreeTableNode(item));
        }

        setRoot(root);
        reload();
    }

    @Override
    public void setData(List<T> newData) {
        this.data = newData;
        buildTree();
    }
}
