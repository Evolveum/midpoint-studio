package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.openapi.project.Project;

public class ConnectorGeneratorDialogContext {

    private final Project project;
    private ConnectorDevelopmentType connectorDevelopmentType;
    private ConnDevDiscoverDocumentationResultType connDevDiscoverDocumentationResultType;

    public ConnectorGeneratorDialogContext(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public ConnectorDevelopmentType getConnectorDevelopmentType() {
        return connectorDevelopmentType;
    }

    public void setConnectorDevelopmentType(ConnectorDevelopmentType connectorDevelopmentType) {
        this.connectorDevelopmentType = connectorDevelopmentType;
    }

    public ConnDevDiscoverDocumentationResultType getConnDevDiscoverDocumentationResultType() {
        return connDevDiscoverDocumentationResultType;
    }

    public void setConnDevDiscoverDocumentationResultType(ConnDevDiscoverDocumentationResultType connDevDiscoverDocumentationResultType) {
        this.connDevDiscoverDocumentationResultType = connDevDiscoverDocumentationResultType;
    }
}
