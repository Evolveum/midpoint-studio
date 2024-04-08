package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.action.task.CleanupReloadNamesTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MissingRefObjectsPanel extends JPanel implements Disposable {

    private final Project project;

    private MissingRefObjectsTable table;

    public MissingRefObjectsPanel(@NotNull Project project) {
        this.project = project;

        setLayout(new BorderLayout());

        initLayout();
    }

    @Override
    public void dispose() {
        // todo implement
    }

    public List<MissingRefObject> getData() {
        return table.getTableModel().getData();
    }

    public void setData(List<MissingRefObject> data) {
        table.getTableModel().setData(data);

        TreeUtil.expandAll(table.getTree());
    }

    private void initLayout() {
        table = new MissingRefObjectsTable();

        add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER);

        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction(
                "Expand All",
                AllIcons.Actions.Expandall,
                e -> TreeUtil.expandAll(table.getTree()));
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction(
                "Collapse All",
                AllIcons.Actions.Collapseall,
                e -> TreeUtil.collapseAll(table.getTree(), -1));
        group.add(collapseAll);

        group.add(new Separator());

        AnAction remove = MidPointUtils.createAnAction(
                "Remove selected",
                AllIcons.General.Remove,
                e -> removeItems());
        group.add(remove);

        group.add(new Separator());

        AnAction reload = MidPointUtils.createAnAction(
                "Reload names",
                AllIcons.Actions.Refresh,
                e -> reloadName());
        group.add(reload);

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("MissingRefObjectsToolbar", group, true);
        toolbar.setTargetComponent(this);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void removeItems() {
        int[] selected = table.getSelectedRows();

        table.getTableModel().removeNodes(selected);
    }

    private void reloadName() {
        Environment env = EnvironmentService.getInstance(project).getSelected();

        CleanupReloadNamesTask task = new CleanupReloadNamesTask(project, table);
        task.setEnvironment(env);
        ProgressManager.getInstance().run(task);
    }
}
