package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.resource.ResourceWizard;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaFileType;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
        if (evt.getProject() == null) {
            return;
        }

//        Project project = evt.getProject();
//
//        ResourceWizard wizard = ResourceWizard.createWizard(project);
//        wizard.showAndGet();

        RunnableUtils.executeWithPluginClassloader(() -> {

            EnvironmentService es = EnvironmentService.getInstance(evt.getProject());
            MidPointClient client = new MidPointClient(evt.getProject(), es.getSelected(), true, true);
            Map<SchemaFileType, String> result = client.getExtensionSchemas();
            System.out.println(result);
            return result;
        });
    }
}
