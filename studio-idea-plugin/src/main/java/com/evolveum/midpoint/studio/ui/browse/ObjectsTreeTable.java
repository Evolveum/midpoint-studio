package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.ToolbarAction;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.TreeTableSpeedSearch;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.UIAction;

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

        this.tableHeader.setReorderingAllowed(false);

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
        group.add(new ToolbarAction("Copy oids", e -> copySelectedObjectOids()));
        group.add(new ToolbarAction("Copy names", e -> copySelectedObjectNames()));

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
