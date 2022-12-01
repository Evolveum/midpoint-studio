package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.ui.GeneralConfiguration;
import com.evolveum.midpoint.studio.ui.FullConfigurationPanel;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    private boolean isPluginVersionRelease = true;

    public TestAction() {
        super("MidPoint Test Action");

        IdeaPluginDescriptor descriptor = PluginManager.getInstance()
                .findEnabledPlugin(PluginId.getId(MidPointConstants.PLUGIN_ID));
        if (descriptor == null) {
            return;
        }

        String version = descriptor.getVersion();

        // e.g. it's release like "4.4.0" and not snapshot "4.4.0-snapshot-250" or other non released version
        isPluginVersionRelease = !version.contains("-");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (isPluginVersionRelease) {
            e.getPresentation().setVisible(false);
            return;
        }

        boolean internal = ApplicationManagerEx.getApplicationEx().isInternal();
        e.getPresentation().setVisible(internal);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();
        if (project == null) {
            return;
        }

        DialogWrapper dialog = new TestDialog(evt.getProject());
        dialog.setSize(1000, 500);
        dialog.showAndGet();
    }

    private static class TestDialog extends DialogWrapper {

        private Project project;

        public TestDialog(Project project) {
            super(false);

            this.project = project;
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            MidPointService mm = MidPointService.getInstance(project);
            EnvironmentService em = EnvironmentService.getInstance(project);

            FullConfigurationPanel gcp = new FullConfigurationPanel(project, new GeneralConfiguration(), mm.getSettings(), em.getFullSettings());
            return gcp.createPanel();
        }
    }
}
