package com.evolveum.midpoint.studio.ui.smart.suggestion.component.model;

import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.List;

public class SmartSuggestionTableModel<T> extends DefaultTreeTableModel<List<SmartSuggestionObject<T>>> {

    List<ColumnInfo> columns;
    List<SmartSuggestionObject<T>> data;

    public SmartSuggestionTableModel(List<ColumnInfo> columns) {
        super(columns);
        this.columns = columns;
    }

    private void buildTree() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();

        for (int i = 0; i < root.getChildCount(); i++) {
            root.remove(i);
        }

        for (SmartSuggestionObject<T> item : data) {
            root.add(new DefaultMutableTreeTableNode(item));
        }

        setRoot(root);
        reload();
    }

    @Override
    public void setData(List<SmartSuggestionObject<T>> newData) {
        this.data = newData;
        buildTree();
    }
}
