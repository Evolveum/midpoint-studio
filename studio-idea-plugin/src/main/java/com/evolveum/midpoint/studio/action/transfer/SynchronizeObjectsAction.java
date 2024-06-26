package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationManager;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SynchronizeObjectsAction extends AnAction implements DumbAware {

    private static final String ACTION_NAME = "Synchronize Objects";

    public SynchronizeObjectsAction() {
        super(ACTION_NAME, null, AllIcons.Actions.Diff);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile[] selectedFiles = UIUtil.invokeAndWaitIfNeeded(() -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(project, ACTION_NAME, ACTION_NAME,
                    "No files matched for " + ACTION_NAME + " (xml)", NotificationType.WARNING);
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        if (env == null) {
            MidPointUtils.publishNotification(project, ACTION_NAME, ACTION_NAME,
                    "No environment selected", NotificationType.ERROR);
            return;
        }

        SynchronizationManager.get(project).synchronize(toProcess, env);
    }
}
