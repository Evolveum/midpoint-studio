package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.action.task.CreateConnectorTask;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.LoadingPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CreateConnectorPanel extends JBPanel<CreateConnectorPanel> implements WizardContent {

    private final ConnectorGeneratorDataModel dialogContext;

    public CreateConnectorPanel(ConnectorGeneratorDataModel dialogContext) {
        this.dialogContext = dialogContext;

    }

    @Override
    public void afterChangeAction() {
        var loadingPanel = new LoadingPanel("Creating Connector...", """
                    We use the connector's basic information to create a test instance for development and testing purposes.""",
                null);
        add(loadingPanel);

        new CreateConnectorTask(
                dialogContext.getProject(),
                () -> DataContext.EMPTY_CONTEXT,
                dialogContext.getConnectorDevelopmentType().getOid()
        ) {
            @Override
            public void onSuccess() {
                super.onSuccess();
                var connDevCreateConnectorResultType = getConnDevCreateConnectorResultType();
                dialogContext.setConnDevCreateConnectorResultType(connDevCreateConnectorResultType);
                remove(loadingPanel);
                revalidate();
                repaint();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                super.onThrowable(error);
            }

            @Override
            public void onFinished() {
                super.onFinished();
            }
        }.queue();

//        if (dialogContext.getConnDevDiscoverDocumentationResultType() == null &&
//                dialogContext.getConnectorDevelopmentType() != null)
//        {
//            var loadingPanel = new LoadingPanel("Creating Connector...", """
//                    We use the connector's basic information to create a test instance for development and testing purposes.""",
//                    null);
//            add(loadingPanel);
//
//            new CreateConnectorTask(
//                    dialogContext.getProject(),
//                    () -> DataContext.EMPTY_CONTEXT,
//                    dialogContext.getConnectorDevelopmentType().getOid()
//            ) {
//                @Override
//                public void onSuccess() {
//                    super.onSuccess();
//                    var connDevCreateConnectorResultType = getConnDevCreateConnectorResultType();
//                    dialogContext.setConnDevCreateConnectorResultType(connDevCreateConnectorResultType);
//                    remove(loadingPanel);
//                }
//
//                @Override
//                public void onThrowable(@NotNull Throwable error) {
//                    super.onThrowable(error);
//                }
//
//                @Override
//                public void onFinished() {
//                    super.onFinished();
//                }
//            }.queue();
//        }
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
