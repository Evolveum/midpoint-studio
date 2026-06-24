package com.evolveum.midpoint.studio.ui.connector.generator.step;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractSmartIntegrationOperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ConnectorGeneratorGeneralWizardStep extends StepAdapter {

    protected final Logger log = Logger.getInstance(this.getClass());

    private final ConnectorGeneratorWizard wizardContext;
    private final MidPointClient client;
    private ConnectorGeneratorDataModel dataModel;
    private GenerateConnectorBadge.State state;
    protected boolean initialized = false;
    private final boolean isHeader;
    private boolean canGoNext = false;

    public ConnectorGeneratorGeneralWizardStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        this.wizardContext = wizardContext;
        this.client = client;
        this.dataModel = dataModel;
        this.state = state;
        this.isHeader = isHeader;
    }

    public MidPointClient getClient() {
        return client;
    }

    public ConnectorGeneratorDataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ConnectorGeneratorWizard getWizardContext() {
        return wizardContext;
    }

    public GenerateConnectorBadge.State getState() {
        return state;
    }

    public void setState(GenerateConnectorBadge.State state) {
        this.state = state;
    }

    protected void canGoNext(boolean canGoNext) {
        this.canGoNext = canGoNext;
    }

    public boolean isCanGoNext() {
        return canGoNext;
    }

    public boolean isHeader() {
        return isHeader;
    }

    protected ConnectorDevelopmentType upsertConnectorDevelopmentType(
            @NotNull ConnectorDevelopmentType connDevConnectorType
    ) throws RuntimeException, SchemaException, AuthenticationException, IOException {
        // FIXME processing error message
        return getClient().upsert(connDevConnectorType.asPrismObject(), null);
    }

    protected SmartIntegrationOperationStatusInfoType getStatusInfo(String token) throws SchemaException, AuthenticationException, IOException {
        return null;
    }

    protected CompletableFuture<AbstractSmartIntegrationOperationResultType> getResult(
        String token
    ) throws CompletionException {

        CompletableFuture<AbstractSmartIntegrationOperationResultType> future =
                new CompletableFuture<>();

        if (token == null) {
            future.completeExceptionally(
                    new IllegalStateException("Submit operation failed. Token is null.")
            );

            return future;
        }

        var scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                var statusInfo = getStatusInfo(token);

                // FIXME unknown status task
                //
                if (statusInfo.getStatus().equals(OperationResultStatusType.UNKNOWN)) {
                    return;
                }

                if (!statusInfo.getStatus().equals(OperationResultStatusType.IN_PROGRESS)) {
                    if (statusInfo.getStatus().equals(OperationResultStatusType.SUCCESS)) {
                        future.complete(statusInfo.getResult());
                    } else {
                        future.completeExceptionally(
                                new RuntimeException(
                                        "Operation failed: " + statusInfo.getStatus() +
                                        (statusInfo.getMessage() != null  ?  ", Message: " + statusInfo.getMessage() : "")
                                )
                        );
                    }
                    scheduler.shutdown();
                }
            } catch (Exception e) {
                log.error(e);
                future.completeExceptionally(e);
                scheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        return future;
    }

    protected void printWaitingPanel(JPanel mainPanel, StatusPanel statusPanel, String title, String description) {
        mainPanel.removeAll();
        mainPanel.add(statusPanel.showLoadingPanel(
                title,
                description,
                0
        ));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    protected void printAlertPanel(JPanel mainPanel, StatusPanel statusPanel, String title, String description) {

//        if (ApplicationManager.getApplication().isDispatchThread()) {
//            System.out.println("Running on EDT");
//        } else {
//            System.out.println("Running on background thread");
//        }

        mainPanel.removeAll();
        mainPanel.add(statusPanel.showAlertPanel(
                title,
                description,
                new JBColor(Gray._255, Gray._255)
        ));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
