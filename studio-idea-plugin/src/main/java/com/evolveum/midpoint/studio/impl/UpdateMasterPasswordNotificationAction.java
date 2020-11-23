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

    public UpdateMasterPasswordNotificationAction() {
        super("Update master password");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }
        String pwd = Messages.showPasswordDialog(project, "Update stored master password",
                "Update master password", null, new NonEmptyInputValidator());

        if (pwd == null) {
            return;
        }

        EncryptionService service = EncryptionService.getInstance(project);

        try {
            service.changeMasterPassword(null, pwd);

            MidPointUtils.publishNotification(EncryptionService.NOTIFICATION_KEY, "Master Password",
                    "Master password update successful.", NotificationType.INFORMATION);
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(EncryptionService.NOTIFICATION_KEY,
                    "Couldn't open credentials database with master password", ex, new UpdateMasterPasswordNotificationAction());
        }
    }
}
