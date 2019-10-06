package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.ui.OperationResultDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ShowResultNotificationAction extends NotificationAction {

    private OperationResult result;

    public ShowResultNotificationAction(@NotNull OperationResult result) {
        super("Show result");

        this.result = result;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        OperationResultDialog dialog = new OperationResultDialog(result);
        dialog.showAndGet();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
