package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.DocumentationDialog;
import com.evolveum.midscribe.generator.ExportFormat;
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
        GenerateOptions options = new GenerateOptions();     // todo fix
        options.setSourceDirectory(new File("~/IdeaProjects/t67/objects"));
        options.setExportFormat(ExportFormat.HTML);
        options.setAdocOutput(new File("~/IdeaProjects/t67/example.adoc"));
        options.setExportOutput(new File("~/IdeaProjects/t67/example.html"));
//        options.setMidpointClient(); // todo client that is able to fetch connectors

        DocumentationDialog dialog = new DocumentationDialog(options);
        if (!dialog.showAndGet()) {
            return;
        }

//        options = dialog.getOptions();

//        options.setSourceDirectory(new File("./midpoint-project/objects"));
//        options.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
//        File adoc = new File("./local.adoc");
//        options.setOutput(adoc);
//
//        options.setExportFormat(ExportFormat.HTML);

        GenerateDocumentationAction gda = new GenerateDocumentationAction(options);
        ActionManager.getInstance().tryToExecute(gda, evt.getInputEvent(), null, ActionPlaces.UNKNOWN, false);
    }
}
