package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationDirection;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationManager;
import com.evolveum.midpoint.studio.util.MidPointUtils;
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

import javax.swing.*;
import java.util.List;

public class SynchronizeObjectsAction extends AnAction implements DumbAware {

    private final String actionName;

    private final SynchronizationDirection direction;

    public SynchronizeObjectsAction(
            @NotNull String actionName, @NotNull Icon icon, @NotNull SynchronizationDirection direction) {

        super(actionName, null, icon);

        this.actionName = actionName;
        this.direction = direction;
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

        evt.getPresentation().setVisible(MidPointUtils.isDevelopmentMode(true));
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
            MidPointUtils.publishNotification(project, actionName, actionName,
                    "No files matched for " + actionName + " (xml)", NotificationType.WARNING);
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        if (env == null) {
            MidPointUtils.publishNotification(project, actionName, actionName,
                    "No environment selected", NotificationType.ERROR);
            return;
        }

        SynchronizationManager.get(project).synchronize(toProcess, env, direction);
    }
}
