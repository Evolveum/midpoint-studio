/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType

data class ConnectorDevelopmentDataModel (
    var occurredChanges: Boolean = false,
    var connectorDevelopment: ConnectorDevelopmentType
)
