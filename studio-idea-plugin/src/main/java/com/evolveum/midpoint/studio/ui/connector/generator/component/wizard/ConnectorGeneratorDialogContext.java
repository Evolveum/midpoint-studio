package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;

public class ConnectorGeneratorDialogContext {

    private final ConnDevApplicationInfoType connDevApplicationInfoType = new ConnDevApplicationInfoType();

    public ConnDevApplicationInfoType getConnDevApplicationInfoType() {
        return connDevApplicationInfoType;
    }
}
