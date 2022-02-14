package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.DiffLocalTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffLocalAction extends AsyncAction<DiffLocalTask> {

    public static final String ACTION_NAME = "Diff local";

    public DiffLocalAction() {
        super(ACTION_NAME, AllIcons.Actions.Diff);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        Presentation presentation = evt.getPresentation();

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        if (!enabled) {
            presentation.setEnabled(false);
            return;
        }

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);
        if (toProcess.size() != 2) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabled(enabled);
    }

    @Override
    protected DiffLocalTask createTask(AnActionEvent e, Environment env) {
        DiffLocalTask task = new DiffLocalTask(e);
        task.setEnvironment(env);

        return task;
    }
}
