package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;

public record NavigationItem(String name, GenerateConnectorBadge.State state, boolean isHeader) {
}
