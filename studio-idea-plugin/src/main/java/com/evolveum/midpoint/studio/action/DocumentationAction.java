package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.DocumentationDialog;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        GenerateOptions options = new GenerateOptions();     // todo fix
        DocumentationDialog dialog = new DocumentationDialog(options);
        if (!dialog.showAndGet()) {
            return;
        }

        options = dialog.getOptions();

//        options.setSourceDirectory(new File("/Users/lazyman/Work/monoted/projects/ek/git/midpoint-project/objects"));
//        options.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
//        File adoc = new File("./local.adoc");
//        options.setOutput(adoc);
//
//        options.setExportFormat(ExportFormat.HTML);

        GenerateDocumentationAction gda = new GenerateDocumentationAction(options);
        ActionManager.getInstance().tryToExecute(gda, evt.getInputEvent(), null, ActionPlaces.UNKNOWN, false);
    }
}
