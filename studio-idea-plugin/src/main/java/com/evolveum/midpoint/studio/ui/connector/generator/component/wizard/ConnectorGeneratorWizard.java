package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.studio.action.ConnectorProjectGenerator;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.*;
import com.evolveum.midpoint.studio.ui.dialog.WizardStepActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.wizard.WizardAction;

import javax.swing.*;

public class ConnectorGeneratorWizard extends WizardDialog<ConnectorGeneratorDataModel> {

    public ConnectorGeneratorWizard(
            Project project,
            String title,
            WizardStepActionHandler actionHandler,
            boolean navigationBarVisible
    ) {
        super(
                project,
                title,
                new ConnectorGeneratorDataModel(project),
                new WizardStep<>("", WizardStepStatus.NONE, new InitialPanel()),
                actionHandler,
                navigationBarVisible
        );

        setSize(1200, 900);
    }

    @Override
    protected void buildSteps(ConnectorGeneratorDataModel dataModel) {
//        WizardStep<ConnectorGeneratorDialogContext> basicGroup = new WizardStep<>("Basic settings", WizardStepStatus.NONE, new InitialBasicSettingPanel());

        rootStep.addChild(new WizardStep<>(
                "Application identification",
                WizardStepStatus.COMPLETE,
                new ApplicationIdentificationPanel(dataModel)
        ));

        rootStep.addChild(new WizardStep<>(
                "Documentation",
                WizardStepStatus.COMPLETE,
                new DiscoverDocumentationPanel(dataModel)
        ));

        rootStep.addChild(new WizardStep<>(
                "Connector identification",
                WizardStepStatus.COMPLETE,
                new ConnectorIdentificationPanel(dataModel)
        ));

        rootStep.addChild(new WizardStep<>(
                "Creating connector",
                WizardStepStatus.NONE,
                new CreateConnectorPanel(dataModel)
        ));
    }

    @Override
    protected void onFinish(ConnectorGeneratorDataModel dataModel) {
        int result = JOptionPane.showConfirmDialog(
                null,
                null,
                "Configure Project",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            var connector = dataModel.getConnectorDevelopmentType().getConnector();
            VirtualFile baseDir = new LightVirtualFile(
                    connector.getDisplayName().getNorm(),
                    connector.getArtifactId()
            );

            new ConnectorProjectGenerator(dataModel).generateProject(dataModel.getProject(), baseDir,  null, (Module) new EmptyProgressIndicator());
        }
    }
}
