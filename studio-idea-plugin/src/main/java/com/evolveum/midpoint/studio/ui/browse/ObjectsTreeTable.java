package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.TreeTableSpeedSearch;
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

public class ObjectsTreeTable extends DefaultTreeTable<ObjectsTreeTableModel> {

    public ObjectsTreeTable() {
        super(new ObjectsTreeTableModel());

        setupComponent();
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

        setupPopupMenu();
    }

    private void setupPopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AnAction("Copy oids") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copySelectedObjectOids();
            }
        });
        group.add(new AnAction("Copy names") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copySelectedObjectNames();
            }
        });
        ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu("ObjectTreeTablePopupMenu", group);
        setComponentPopupMenu(menu.getComponent());
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
