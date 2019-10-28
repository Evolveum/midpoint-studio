package com.evolveum.midpoint.studio.action.browse;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SearchAction extends AnAction implements UpdateInBackground {

    public SearchAction() {
        super("Search", "Search", AllIcons.Actions.Find);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();

        Task.Backgroundable task = new Task.Backgroundable(project, "Searching objects", true) {

            @Override
            public void run(ProgressIndicator indicator) {
                doSearch(evt, indicator);
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                super.onThrowable(error);

                SearchAction.this.onThrowable(error);
            }

            @Override
            public void onFinished() {
                super.onFinished();

                SearchAction.this.onFinished();
            }
        };

        ProgressManager.getInstance().run(task);

        task.onFinished();
    }

    private void doSearch(AnActionEvent evt, ProgressIndicator indicator) {
        // todo implement progress support
        String envName = "";
        indicator.setText("Searching objects in " + envName + " MidPoint");


    }

    protected void onThrowable(@NotNull Throwable error) {
        // todo implement
    }

    protected void onFinished() {
        // todo implement
    }
}
