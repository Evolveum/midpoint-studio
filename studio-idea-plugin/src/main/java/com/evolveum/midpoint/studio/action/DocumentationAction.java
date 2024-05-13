package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.DocumentationTask;
import com.evolveum.midpoint.studio.impl.DocGeneratorOptions;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.ui.DocumentationDialog;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationAction extends AsyncAction<DocumentationTask> {

    private GenerateOptions options;

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        MidPointService mm = MidPointService.get(evt.getProject());
        MidPointConfiguration settings = mm.getSettings();

        DocGeneratorOptions opts = settings.getDocGeneratorOptions();

        if (opts == null) {
            opts = DocGeneratorOptions.createDefaultOptions(evt.getProject());
        }

        DocumentationDialog dialog = new DocumentationDialog(evt.getProject(), opts);
        if (!dialog.showAndGet()) {
            return;
        }

        opts = dialog.getOptions();
        settings.setDocGeneratorOptions(opts);
        mm.settingsUpdated();

        GenerateOptions options = DocGeneratorOptions.buildGenerateOptions(opts);

        File exportOutput = opts.getExportOutput();
        File adocOutput = new File(exportOutput.getParent(), exportOutput.getName() + ".adoc");
        options.setAdocOutput(adocOutput);

        this.options = options;

        super.actionPerformed(evt);
    }

    @Override
    protected DocumentationTask createTask(AnActionEvent e, Environment env) {
        DocumentationTask task = new DocumentationTask(e.getProject(), e::getDataContext, options);
        task.setEnvironment(env);

        return task;
    }
}
