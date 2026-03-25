package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class ConnectorGeneratorAction extends AnAction {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        EnvironmentService em = EnvironmentService.getInstance(Objects.requireNonNull(project));
        Environment env = em.getSelected();

        new ConnectorGeneratorWizard(
                anActionEvent.getProject(),
                "Connector generator",
                new ConnectorGeneratorDialogContext(project, env),
                new DialogWindowActionHandler() {

                    @Override
                    public String getOkButtonTitle() {
                        return "Allow and continue";
                    }

                    @Override
                    public void onOk() {
                        System.out.println("TOUCH OK!");
                    }
                },
                true
        ).show();
    }
}
