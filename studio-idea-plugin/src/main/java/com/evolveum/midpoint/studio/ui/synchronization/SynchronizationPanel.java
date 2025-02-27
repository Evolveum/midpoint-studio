package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.ui.ToolbarAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SynchronizationPanel extends BorderLayoutPanel {

    private final Project project;

    private SynchronizationTree tree;

    public SynchronizationPanel(@NotNull Project project) {
        this.project = project;

        initLayout();
    }

    private void initLayout() {
        JComponent mainToolbar = initMainToolbar(this);
        add(mainToolbar, BorderLayout.NORTH);

        tree = new SynchronizationTree(project, new SynchronizationTreeModel());

        add(ScrollPaneFactory.createScrollPane(tree, true), BorderLayout.CENTER);

        JComponent buttons = initButtons();
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel initButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton saveFiles = new JButton("Save local");
        saveFiles.setDefaultCapable(true);
        saveFiles.addActionListener(e -> saveLocallyPerformed());
        panel.add(saveFiles);

        JButton updateRemote = new JButton("Update remote");
        updateRemote.addActionListener(e -> updateRemotePerformed());
        panel.add(updateRemote);

        return panel;
    }

    public void expandTree() {
        TreeUtil.expandAll(tree);
    }

    private JComponent initMainToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new ToolbarAction(
                "Expand All", AllIcons.Actions.Expandall, e -> TreeUtil.expandAll(tree)));
        group.add(new ToolbarAction(
                "Collapse All", AllIcons.Actions.Collapseall, e -> TreeUtil.collapseAll(tree, true, 1)));

        group.add(new Separator());

        group.add(new ToolbarAction("Refresh", AllIcons.Actions.Refresh, e -> refreshPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = tree.getCheckedNodes(Object.class, null).length != 0;
                e.getPresentation().setEnabled(enabled);
            }
        });

        group.add(new ToolbarAction("Upload (Full Processing)", AllIcons.Actions.Upload, e -> uploadPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = tree.getCheckedNodes(Object.class, null).length != 0;
                e.getPresentation().setEnabled(enabled);
            }
        });

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("diff-panel-toolbar", group, true);
        toolbar.setTargetComponent(parent);

        return toolbar.getComponent();
    }

    public SynchronizationTreeModel getModel() {
        return (SynchronizationTreeModel) tree.getModel();
    }

    private void uploadPerformed() {
        int result = MidPointUtils.showConfirmationDialog(
                project, "Full upload will upload current objects as they are stored in file. Do you want to continue?",
                "Confirm upload", "Upload", "Cancel");

        if (result != MessageDialog.OK_EXIT_CODE) {
            return;
        }

        SynchronizationItem[] userObjects = tree.getCheckedNodes(SynchronizationItem.class, null);
        List<SynchronizationItem> items = Arrays.asList(userObjects);

        SynchronizationSession<?> session = getSession();

        SynchronizationUploadTask task = new SynchronizationUploadTask(project, session, items);
        task.setEnvironment(session.getEnvironment());
        ProgressManager.getInstance().run(task);
    }

    private void refreshPerformed() {
        int result = MidPointUtils.showConfirmationDialog(
                project, "Refreshing synchronization will discard all unsaved changes. Do you want to continue?",
                "Confirm remove", "Refresh", "Cancel");

        if (result != MessageDialog.OK_EXIT_CODE) {
            return;
        }

        SynchronizationItem[] userObjects = tree.getCheckedNodes(SynchronizationItem.class, null);
        List<SynchronizationItem> items = Arrays.asList(userObjects);

        SynchronizationSession<?> session = getSession();

        SynchronizationRefreshTask task = new SynchronizationRefreshTask(project, session, items);
        task.setEnvironment(session.getEnvironment());
        ProgressManager.getInstance().run(task);
    }

    private List<SynchronizationObjectItem> computeCheckedObjectItems(Object[] userObjects) {
        List<SynchronizationObjectItem> objects = new ArrayList<>();

        for (Object userObject : userObjects) {
            if (userObject instanceof SynchronizationFileItem<?> sfi) {
                sfi.getObjects().stream()
                        .filter(o -> !objects.contains(o))
                        .forEach(objects::add);
            } else if (userObject instanceof SynchronizationObjectItem oi) {
                if (!objects.contains(oi)) {
                    objects.add(oi);
                }
            }
        }

        return objects;
    }

    private void saveLocallyPerformed() {
        Object[] userObjects = tree.getCheckedNodes(Object.class, null);

        List<SynchronizationObjectItem> items = computeCheckedObjectItems(userObjects);

        SynchronizationSession<?> session = getSession();
        session.saveLocally(items);

        // todo we have to refresh all file nodes related to this save
        getModel().nodesChanged(userObjects);
    }

    private SynchronizationSession<?> getSession() {
        SynchronizationManager sm = SynchronizationManager.get(project);
        return sm.getSession();
    }

    private void updateRemotePerformed() {
        Object[] userObjects = tree.getCheckedNodes(Object.class, null);

        List<SynchronizationObjectItem> items = computeCheckedObjectItems(userObjects);

        SynchronizationSession<?> session = getSession();
        session.updateRemote(items);

        getModel().nodesChanged(userObjects);
    }
}
