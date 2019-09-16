package com.evolveum.midpoint.studio.action.environment;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.ui.EnvironmentListDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EditEnvironments extends AnAction {

    public static final String ACTION_ID = MidPointConstants.ACTION_ID_PREFIX + EditEnvironments.class.getSimpleName();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showDialog(e.getProject());
    }

    @Nullable
    private EnvironmentListDialog showDialog(Project project) {
        final Ref<EnvironmentListDialog> dialog = Ref.create();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            dialog.set(new EnvironmentListDialog(project));
            dialog.get().show();
        }, ModalityState.any());

        return dialog.get();
    }
}
