package com.evolveum.midpoint.studio.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ShowExceptionNotificationAction extends NotificationAction {

    private Environment environment;

    private Class clazz;

    private String message;

    private Exception exception;

    public ShowExceptionNotificationAction(String message, @NotNull Exception exception, Class clazz, Environment environment) {
        super("Show exception");

        this.message = message;
        this.exception = exception;
        this.clazz = clazz;
        this.environment = environment;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt, @NotNull Notification notification) {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message).append('\n');
        }
        sb.append(ExceptionUtil.getThrowableText(exception));

        Logger.getInstance(clazz).error(message, exception);

        MidPointService mm = MidPointService.getInstance(evt.getProject());
        mm.printToConsole(environment, clazz, sb.toString());
        mm.focusConsole();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
