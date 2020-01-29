package com.evolveum.midpoint.studio.action.browse;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BackgroundAction extends AnAction implements UpdateInBackground {

    private String taskTitle;

    private Task.Backgroundable task;

    private boolean running;

    public BackgroundAction(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public BackgroundAction(String text, Icon icon, String taskTitle) {
        this(text, text, icon, taskTitle);
    }

    public BackgroundAction(String text, String description, Icon icon, String taskTitle) {
        super(text, description, icon);

        this.taskTitle = taskTitle;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();

        task = new Task.Backgroundable(project, taskTitle, true) {

            @Override
            public void run(ProgressIndicator indicator) {
                running = true;

                executeOnBackground(evt, indicator);
            }

            @Override
            public void onCancel() {
                super.onCancel();

                BackgroundAction.this.onCancel();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();

                BackgroundAction.this.onSuccess();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                super.onThrowable(error);

                BackgroundAction.this.onThrowable(error);
            }

            @Override
            public void onFinished() {
                super.onFinished();

                BackgroundAction.this.onFinished();

                running = false;
            }
        };

        ProgressManager.getInstance().run(task);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setEnabled(isEnabled());
    }

    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {

    }

    protected boolean isEnabled() {
        return !running;
    }

    protected void onThrowable(@NotNull Throwable error) {

    }

    protected void onFinished() {

    }

    protected void onCancel() {

    }

    protected void onSuccess() {

    }

    public boolean isRunning() {
        return running;
    }
}
