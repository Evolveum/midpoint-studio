package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.intellij.openapi.project.Project;

public class ConnectorGeneratorDialogContext {

    private final MidPointClient client;

    public ConnectorGeneratorDialogContext(Project project, Environment environment) {
        this.client = new MidPointClient(project, environment);
    }

    public MidPointClient getClient() {
        return client;
    }

    private final ConnDevApplicationInfoType connDevApplicationInfoType = new ConnDevApplicationInfoType();

    public ConnDevApplicationInfoType getConnDevApplicationInfoType() {
        return connDevApplicationInfoType;
    }
}
