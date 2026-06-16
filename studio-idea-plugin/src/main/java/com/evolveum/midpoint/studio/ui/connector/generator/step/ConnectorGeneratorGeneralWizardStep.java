package com.evolveum.midpoint.studio.ui.connector.generator.step;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectorGeneratorGeneralWizardStep extends StepAdapter {

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

    protected void upsertConnectorDevelopmentType(@NotNull ConnectorDevelopmentType connDevConnectorType) {

        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                getDataModel().connectorDevelopmentType = getClient().upsert(connDevConnectorType.asPrismObject(), null);
            } catch (Exception e) {
                log.error("Couldn't upsert connector development type", e);
                throw new RuntimeException(e);
            }
        }, "Update ConnectorDevelopmentType Object", true, getClient().getProject());
    }

    protected void printAlert(JPanel mainPanel, StatusPanel statusPanel, String title, String msg) {
        mainPanel.removeAll();
        mainPanel.add(statusPanel.showAlertPanel(
                title,
                msg,
                new JBColor(Gray._255, Gray._255)
        ));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
