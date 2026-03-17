package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.InitialPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.BasicSettingPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.ConnectorIdentificationPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.CreatingConnectorPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection.AuthMethodSupportPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection.BaseUrlSpecificationPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection.CredentialsPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection.TestConnectionPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.next.NextPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass.*;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.*;
import com.intellij.openapi.project.Project;

public class ConnectorGeneratorWizard extends WizardDialog<ConnectorGeneratorDialogContext> {

    public ConnectorGeneratorWizard(
            Project project,
            String title,
            ConnectorGeneratorDialogContext dialogWizardContext,
            DialogWindowActionHandler actionHandler,
            boolean navigationMenuVisible
    ) {
        super(
                project,
                title,
                dialogWizardContext,
                new WizardStep<>("", WizardStepStatus.NONE, new InitialPanel()),
                actionHandler,
                navigationMenuVisible
        );
        setSize(1200, 900);
    }

    @Override
    protected void buildSteps(ConnectorGeneratorDialogContext context) {
        WizardStep<ConnectorGeneratorDialogContext> basicGroup = new WizardStep<>("Basic settings", WizardStepStatus.NONE, new InitialPanel());
        basicGroup.addChild(new WizardStep<>(
                "Application identification",
                WizardStepStatus.COMPLETE,
                new BasicSettingPanel(context)
        ));

        basicGroup.addChild(new WizardStep<>(
                "Documentation",
                WizardStepStatus.IN_PROGRESS,
                new DocumentationPanel(context)
        ));

        basicGroup.addChild(new WizardStep<>(
                "Connector identification",
                WizardStepStatus.PENDING,
                new ConnectorIdentificationPanel(context)
        ));

        basicGroup.addChild(new WizardStep<>(
                "Creating connector",
                WizardStepStatus.NONE,
                new CreatingConnectorPanel(context)
        ));

        WizardStep<ConnectorGeneratorDialogContext> connectionGroup = new WizardStep<>("Connection", WizardStepStatus.NONE, new InitialPanel());
        connectionGroup.addChild(new WizardStep<>(
                "Base URL specification",
                WizardStepStatus.NONE,
                new BaseUrlSpecificationPanel(context)
        ));

        connectionGroup.addChild(new WizardStep<>(
                "Auth method support",
                WizardStepStatus.NONE,
                new AuthMethodSupportPanel(context)
        ));

        connectionGroup.addChild(new WizardStep<>(
                "Credentials",
                WizardStepStatus.NONE,
                new CredentialsPanel(context)
        ));

        connectionGroup.addChild(new WizardStep<>(
                "Test connection",
                WizardStepStatus.NONE,
                new TestConnectionPanel(context)
        ));

        WizardStep<ConnectorGeneratorDialogContext> objectClassGroup = new WizardStep<>("Object class: User", WizardStepStatus.NONE, new InitialPanel());
        objectClassGroup.addChild(new WizardStep<>(
                "Object classes",
                WizardStepStatus.NONE,
                new ObjectClassesPanel(context)
        ));

        objectClassGroup.addChild(new WizardStep<>(
                "Schema scripts validation",
                WizardStepStatus.NONE,
                new SchemaScriptValidatorPanel(context)
        ));

        objectClassGroup.addChild(new WizardStep<>(
                "Schema validation",
                WizardStepStatus.NONE,
                new SchemaValidationPanel(context)
        ));

        objectClassGroup.addChild(new WizardStep<>(
                "Search endpoints",
                WizardStepStatus.NONE,
                new SearchEndpointPanel(context)
        ));

        objectClassGroup.addChild(new WizardStep<>(
                "Search all script validation",
                WizardStepStatus.NONE,
                new SearchAllScriptValidationPanel(context)
        ));

        objectClassGroup.addChild(new WizardStep<>(
                "Search results",
                WizardStepStatus.NONE,
                new SearchResultPanel(context)
        ));

        WizardStep<ConnectorGeneratorDialogContext> nextGroup = new WizardStep<>("Next", WizardStepStatus.NONE, new InitialPanel());
        nextGroup.addChild(new WizardStep<>(
                "Next",
                WizardStepStatus.NONE,
                new NextPanel(context)
        ));

        rootStep.addChild(basicGroup);
        rootStep.addChild(connectionGroup);
        rootStep.addChild(objectClassGroup);
        rootStep.addChild(nextGroup);
    }

    @Override
    protected void onFinish(ConnectorGeneratorDialogContext context) {
        // TODO finished implementation
    }
}
