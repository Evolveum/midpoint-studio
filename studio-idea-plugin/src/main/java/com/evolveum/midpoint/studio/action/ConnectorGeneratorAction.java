package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectorGeneratorAction extends AnAction {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        var connectorGeneratorDialogContext = new ConnectorGeneratorDialogContext();

        new ConnectorGeneratorWizard(
                anActionEvent.getProject(),
                "Connector generator",
                connectorGeneratorDialogContext,
                new DialogWindowActionHandler() {

                    @Override
                    public boolean isOkButtonEnabled() {
                        return true;
                    }

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
