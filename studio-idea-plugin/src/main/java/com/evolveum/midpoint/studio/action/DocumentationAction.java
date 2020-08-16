package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.DocGeneratorOptions;
import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.evolveum.midpoint.studio.ui.DocumentationDialog;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        MidPointManager mm = MidPointManager.getInstance(evt.getProject());
        MidPointSettings settings = mm.getSettings();

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

        // todo set custom midpoint client that can fetch for example connectors from environment

        GenerateDocumentationAction gda = new GenerateDocumentationAction(options);
        ActionManager.getInstance().tryToExecute(gda, evt.getInputEvent(), null, ActionPlaces.UNKNOWN, false);
    }
}
