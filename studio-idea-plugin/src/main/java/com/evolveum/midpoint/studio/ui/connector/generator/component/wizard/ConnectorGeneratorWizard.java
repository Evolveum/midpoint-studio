package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.*;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class ConnectorGeneratorWizard extends MidpointWizardDialog<ConnectorGeneratorDialogContext> {

    public ConnectorGeneratorWizard(
            Project project,
            String title,
            ConnectorGeneratorDialogContext dialogWizardContext,
            DialogWindowActionHandler actionHandler,
            boolean navigationMenuVisible
    ) {
        super(project, title, dialogWizardContext, actionHandler, navigationMenuVisible);
        setSize(800, 600);
    }

    @Override
    protected void buildSteps(ConnectorGeneratorDialogContext context) {
        var first = new JPanel(new BorderLayout());
        first.add(new JLabel("FIRST"));

        var second = new JPanel(new BorderLayout());
        second.add(new JLabel("SECOND"));

        var third = new JPanel(new BorderLayout());
        third.add(new JLabel("THIRD"));

        var four = new JPanel(new BorderLayout());
        four.add(new JLabel("FOUR"));

        var five = new JPanel(new BorderLayout());
        five.add(new JLabel("FIVE"));

        var six = new JPanel(new BorderLayout());
        six.add(new JLabel("SIX"));


        NavigationStep basic = new NavigationStep(
                "Basic settings",
                StepStatus.COMPLETE,
                first
        );

        NavigationStep connection = new NavigationStep(
                "Connection",
                StepStatus.COMPLETE,
                second
        );


        NavigationStep connection1 = new NavigationStep(
                "Connection1",
                StepStatus.COMPLETE,
                four
        );


        NavigationStep connection2 = new NavigationStep(
                "Connection2",
                StepStatus.COMPLETE,
                five
        );

        NavigationStep objectGroup = new NavigationStep("Object class: User", StepStatus.PENDING, third);

        objectGroup.addChild(new NavigationStep(
                "Object classes",
                StepStatus.COMPLETE,
                four
        ));

        objectGroup.addChild(new NavigationStep(
                "Schema scripts validation",
                StepStatus.COMPLETE,
                five
        ));

        objectGroup.addChild(new NavigationStep(
                "Schema validation",
                StepStatus.IN_PROGRESS,
                six
        ));

        rootStep.addChild(basic);
        rootStep.addChild(connection);

        rootStep.addChild(connection1);
        rootStep.addChild(connection2);

        rootStep.addChild(objectGroup);
    }

    @Override
    protected void onFinish(ConnectorGeneratorDialogContext context) {
        // TODO finished implementation
    }
}
