package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.action.TestConnectionAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ShowExceptionNotificationAction extends NotificationAction {

    private String message;

    private Exception exception;

    public ShowExceptionNotificationAction(String message, @NotNull Exception exception) {
        super("Show exception");

        this.message = message;
        this.exception = exception;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt, @NotNull Notification notification) {
        MidPointService mm = MidPointService.getInstance(evt.getProject());

        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message).append('\n');
        }
        sb.append(ExceptionUtil.getThrowableText(exception));

        mm.printToConsole(TestConnectionAction.class, sb.toString());
        mm.focusConsole();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
