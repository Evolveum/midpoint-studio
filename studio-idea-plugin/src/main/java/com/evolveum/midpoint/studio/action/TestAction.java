package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.client.ScriptObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends BackgroundAction {

    public TestAction() {
        super("Downloading mapping");
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (evt.getProject() == null) {
            return;
        }

        Project project = evt.getProject();

        try {
            EnvironmentService environmentService = EnvironmentService.getInstance(project);
            Environment env = environmentService.getSelected();

            MidPointClient client = new MidPointClient(project, env, true, false);

            List<ScriptObject> sos = client.getSourceProfileScripts("null");
            if (sos.isEmpty()) {
                return;
            }

            ScriptObject so = sos.get(0);

            RunnableUtils.runWriteActionAndWait(() -> {
                try {
                    VirtualFile lvf = project.getBaseDir().findFileByRelativePath("/src/main/java")
                            .createChildData(this, URLDecoder.decode(so.getClazz(), StandardCharsets.UTF_8) + ".groovy");
                    lvf.setBinaryContent(URLDecoder.decode(so.getKey(), StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8));

                    MidPointUtils.openFile(project, lvf);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
