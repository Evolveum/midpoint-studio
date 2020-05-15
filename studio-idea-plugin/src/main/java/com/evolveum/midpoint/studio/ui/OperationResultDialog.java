package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultDialog extends DialogWrapper {

    private BorderLayoutPanel panel;

    public OperationResultDialog(@NotNull OperationResult result) {
        super(false);

        setTitle(result.getOperation());
        setSize(200, 100);

        this.panel = new BorderLayoutPanel();

        List<TreeTableColumnDefinition<OperationResult, Object>> columns = new ArrayList<>();
        columns.add(new TreeTableColumnDefinition<>("Operation", 150, r -> r.getOperation()));
        columns.add(new TreeTableColumnDefinition<>("Status", 50, r -> r.getStatus()));
        columns.add(new TreeTableColumnDefinition<>("Message", 500, r -> r.getMessage() != null ? r.getMessage() : ""));

        JXTreeTable table = MidPointUtils.createTable(new OperationResultModel(result, columns), (List) columns);
        table.setRootVisible(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JComponent toolbar = initToolbar(table, result);
        this.panel.addToTop(toolbar);
        this.panel.addToCenter(new JBScrollPane(table));

        init();
    }

    private JComponent initToolbar(JXTreeTable table, OperationResult result) {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> table.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> table.collapseAll());
        group.add(collapseAll);

        group.add(new Separator());

        AnAction export = MidPointUtils.createAnAction("Export Result", AllIcons.Actions.Download, e -> download(e, result));
        group.add(export);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("OperationResultDialogToolbar", group, true);
        return toolbar.getComponent();
    }

    private void download(AnActionEvent e, OperationResult result) {
        // todo implement
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
