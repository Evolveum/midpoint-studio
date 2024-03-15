package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.UIAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectsTreeTable extends TreeTable {

    public ObjectsTreeTable() {
        super(new ObjectsTreeTableModel());

        setupComponent();
    }

    @Override
    public ObjectsTreeTableModel getTableModel() {
        return (ObjectsTreeTableModel) super.getTableModel();
    }

    @Override
    public void setTableModel(TreeTableModel model) {
        if (!(model instanceof ObjectsTreeTableModel)) {
            throw new IllegalArgumentException("Model must be instance of ObjectsTreeTableModel");
        }
        super.setTableModel(model);
    }

    private void setupComponent() {
        setRootVisible(false);
        setDragEnabled(false);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setTreeCellRenderer(new NodeRenderer() {

            @Override
            public void customizeCellRenderer(
                    @NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {

                value = getTableModel().getColumnInfo(0).valueOf(value);

                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        });

        this.tableHeader.setReorderingAllowed(false);

        this.columnModel.getColumn(0).setPreferredWidth(350);
        this.columnModel.getColumn(0).setMinWidth(50);

        this.columnModel.getColumn(1).setPreferredWidth(320);
        this.columnModel.getColumn(1).setMinWidth(50);
        this.columnModel.getColumn(1).setMaxWidth(500);

        setupSpeedSearch();

        getActionMap().put("copy", new UIAction("copy") {

            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedObjectOids();
            }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem item = new JMenuItem("Copy oids");
        item.addActionListener(e -> copySelectedObjectOids());
        popup.add(item);

        item = new JMenuItem("Copy names");
        item.addActionListener(e -> copySelectedObjectNames());
        popup.add(item);

        setComponentPopupMenu(popup);
    }

    private void setupSpeedSearch() {
        // todo fix lambda
        TreeTableSpeedSearch search = new TreeTableSpeedSearch(this, p -> p.toString());
        search.setCanExpand(true);
    }

    private void copySelectedObjectNames() {
        List<ObjectType> objects = getTableModel().getSelectedObjects();
        if (objects.isEmpty()) {
            return;
        }

        List<String> names = objects.stream().map(o -> MidPointUtils.getName(o.asPrismObject())).collect(Collectors.toList());
        String text = StringUtils.join(names, '\n');

        putStringToClipboard(text);
    }

    private void copySelectedObjectOids() {
        List<Pair<String, ObjectTypes>> oidTypes = getTableModel().getSelectedOids();

        if (oidTypes.isEmpty()) {
            return;
        }

        List<String> oids = oidTypes.stream().map(p -> p.getFirst()).collect(Collectors.toList());
        String text = StringUtils.join(oids, '\n');

        putStringToClipboard(text);
    }

    private void putStringToClipboard(String str) {
        if (str == null) {
            str = "";
        }

        StringSelection selection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
