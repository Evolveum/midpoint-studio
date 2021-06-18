package com.evolveum.midpoint.studio.ui.resource;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ResourceWizardModel {

    private Project project;

    private ResourceType resource = new ResourceType();

    private ResourceType resourceOnServer;

    private List<ConnectorType> connectors;

    private boolean source;

    private boolean target;

    public ResourceWizardModel(@NotNull Project project) {
        this.project = project;
    }

    public ResourceType getResource() {
        return resource;
    }

    public ResourceType getResourceOnServer() {
        return resourceOnServer;
    }

    public List<ConnectorType> getConnectors() {
        if (connectors == null) {
            connectors = new ArrayList<>();
        }
        return connectors;
    }

    public boolean getSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean getTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    private void refreshConnectors() {

    }
}
