package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.*;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.*;
import com.intellij.openapi.project.Project;

public class ConnectorGeneratorWizard extends WizardDialog<ConnectorGeneratorDialogContext> {

    public ConnectorGeneratorWizard(
            Project project,
            String title,
            DialogWindowActionHandler actionHandler,
            boolean navigationBarVisible
    ) {
        super(
                project,
                title,
                new ConnectorGeneratorDialogContext(project),
                new WizardStep<>("", WizardStepStatus.NONE, new InitialPanel()),
                actionHandler,
                navigationBarVisible
        );

        setSize(1200, 900);
    }

    @Override
    protected void buildSteps(ConnectorGeneratorDialogContext dialogContext) {
//        WizardStep<ConnectorGeneratorDialogContext> basicGroup = new WizardStep<>("Basic settings", WizardStepStatus.NONE, new InitialBasicSettingPanel());

        rootStep.addChild(new WizardStep<>(
                "Application identification",
                WizardStepStatus.COMPLETE,
                new ApplicationIdentificationPanel(dialogContext)
        ));

        rootStep.addChild(new WizardStep<>(
                "Documentation",
                WizardStepStatus.IN_PROGRESS,
                new DiscoverDocumentationPanel(dialogContext)
        ));

        rootStep.addChild(new WizardStep<>(
                "Connector identification",
                WizardStepStatus.PENDING,
                new ConnectorIdentificationPanel(dialogContext)
        ));

        rootStep.addChild(new WizardStep<>(
                "Creating connector",
                WizardStepStatus.NONE,
                new CreateConnectorPanel(dialogContext)
        ));
    }

    @Override
    protected void onFinish(ConnectorGeneratorDialogContext dialogContext) {
        // TODO finished implementation create connector project and open new
    }
}
