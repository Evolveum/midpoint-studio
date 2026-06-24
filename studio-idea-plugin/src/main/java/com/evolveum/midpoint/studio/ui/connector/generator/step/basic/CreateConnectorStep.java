package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class CreateConnectorStep extends ConnectorGeneratorGeneralWizardStep {

    private CreateConnector createConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public CreateConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        mainPanel.setName("Creating Connector");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;

            setState(GenerateConnectorBadge.State.IN_PROGRESS);

            createConnector = new CreateConnector();

            ProgressManager.getInstance().run(new Task.Backgroundable(getClient().getProject(),
                    "CreateConnector submit operation",
                    true
            ) {

                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = getClient().submitOperationCreateConnector(getDataModel().connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {
                    var statusPanel = createConnector.getStatusPanel();

                    if (token == null) {

                        printAlertPanel(mainPanel, statusPanel, "Error", "Token is Null");
                        return;
                    }

                    printWaitingPanel(
                            mainPanel,
                            statusPanel,
                            "Creating Connector...",
                            """
                                  We use the connector's basic information to create a test instance for development and testing purposes.
                                  """
                    );

                    statusPanel.getElapsedLabel().start();

                    getResult(token).whenComplete((statusInfoResult, ex) -> {
                        if (ex != null) {
                            printAlertPanel(mainPanel, statusPanel, "Error", ex.getMessage());
                        } else {
                            var result = statusInfoResult.getConnDevCreateConnectorResult();

                            if (result != null && result.getConnectorRef() != null) {

                                try {

                                    if (createResourceObject(result.getConnectorRef()) == null) {
                                        throw new Exception("Error creating resource object for connector: " + result.getConnectorRef());
                                    } else {
                                        printAlertPanel(
                                                mainPanel,
                                                statusPanel,
                                                OperationResultStatusType.SUCCESS.name(),
                                                "Successfully Generated Connector Development"
                                        );

                                        canGoNext(true);
                                        getWizardContext().updateWizardButtons();
                                        setState(GenerateConnectorBadge.State.COMPLETE);
                                    }
                                } catch (Exception e) {
                                    printAlertPanel(mainPanel, statusPanel, OperationResultStatusType.UNKNOWN.name(), e.getMessage());
                                }
                            } else {

                                printAlertPanel(
                                        mainPanel,
                                        statusPanel,
                                        OperationResultStatusType.UNKNOWN.name(),
                                        "ConnectorRef Null"
                                );
                            }
                        }

                        statusPanel.getElapsedLabel().stop();
                    });

                    super.onSuccess();
                }
            });
        }

        super._init();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public SmartIntegrationOperationStatusInfoType getStatusInfo(
            String token
    ) throws SchemaException, AuthenticationException, IOException {
        return getClient().getStatusInfoCreateConnector(token);
    }

    private UploadResponse createResourceObject(
            @NotNull ObjectReferenceType objectReferenceType
    ) throws SchemaException, AuthenticationException, IOException {

        var prismContext = getClient().getPrismContext();

        if (prismContext == null) {
            return null;
        }

        PrismObject<ResourceType> resourceObject = prismContext.createObject(ResourceType.class);
        ResourceType resourceType = resourceObject.asObjectable();
        resourceType.setName(getDataModel().connectorDevelopmentType.getName());
        ObjectReferenceType clonedRef = objectReferenceType.clone();
        resourceType.setConnectorRef(clonedRef);

        return getClient().upload(resourceType.asPrismObject(), null);
    }
}
