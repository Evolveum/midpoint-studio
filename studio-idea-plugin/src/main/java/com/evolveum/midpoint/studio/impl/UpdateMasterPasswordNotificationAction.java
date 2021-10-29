package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UpdateMasterPasswordNotificationAction extends NotificationAction {

    private boolean masterPwdExists;

    public UpdateMasterPasswordNotificationAction(boolean masterPwdExists) {
        super("Update master password");

        this.masterPwdExists = masterPwdExists;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }

        String content = masterPwdExists ? "Update stored master password" : "Set new master password";

        String pwd = Messages.showPasswordDialog(project, content, "Master password", null, new NonEmptyInputValidator());

        if (pwd == null) {
            return;
        }

        EncryptionService service = EncryptionService.getInstance(project);

        try {
            service.changeMasterPassword(null, pwd);

            MidPointUtils.publishNotification(project, EncryptionService.NOTIFICATION_KEY, "Master Password",
                    "Master password update successful.", NotificationType.INFORMATION);
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(project, null, UpdateMasterPasswordNotificationAction.class,
                    EncryptionService.NOTIFICATION_KEY, "Couldn't open credentials database with master password",
                    ex, new UpdateMasterPasswordNotificationAction(true));
        }
    }
}
