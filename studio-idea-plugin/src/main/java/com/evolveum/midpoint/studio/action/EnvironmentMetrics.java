package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.metrics.MetricsManager;
import com.evolveum.midpoint.studio.impl.metrics.MetricsSession;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentMetrics extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MetricsManager mm = MetricsManager.getInstance(e.getProject());

        MetricsSession session = mm.createSession();

        VirtualFile file = session.getFile();
        new OpenFileDescriptor(e.getProject(), file).navigate(true);

        ApplicationManager.getApplication().executeOnPooledThread(() -> session.start());
    }
}
