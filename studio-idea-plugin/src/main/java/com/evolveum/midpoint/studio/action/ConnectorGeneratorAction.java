package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.dialog.WizardStepActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ConnectorGeneratorAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {


        new ConnectorGeneratorWizard(anActionEvent.getProject()).show();


//        new ConnectorGeneratorWizard(
//                anActionEvent.getProject(),
//                "Connector generator",
//                new WizardStepActionHandler() {
//                    @Override
//                    public String getOkButtonTitle() {
//                        return "Allow and continue";
//                    }
//
//                    @Override
//                    public void onOk() {
//                        System.out.println("TOUCH OK!");
//                    }
//                },
//                true
//        ).show();
    }
}
