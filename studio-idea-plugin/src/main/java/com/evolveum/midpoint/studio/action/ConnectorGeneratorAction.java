package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectorGeneratorAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        new ConnectorGeneratorWizard(
                anActionEvent.getProject(),
                "Connector generator",
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
