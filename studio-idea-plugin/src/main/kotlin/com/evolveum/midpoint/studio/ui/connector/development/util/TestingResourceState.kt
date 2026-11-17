/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.util

import com.evolveum.midpoint.xml.ns._public.common.common_3.AvailabilityStatusType

/**
 * Snapshot of the testing resource used to decide which wizard steps are already done: the connector configuration
 * properties with a value, keyed by local name, and the last availability status.
 */
class TestingResourceState(
    val configuration: Map<String, Any?>,
    val availabilityStatus: AvailabilityStatusType?
) {

    fun has(property: String): Boolean = configuration[property] != null

    fun hasAll(properties: Collection<String>): Boolean = properties.all { has(it) }

    /** The last connection test passed, the web GUI treats the whole connection group as complete then. */
    val isUp: Boolean
        get() = availabilityStatus == AvailabilityStatusType.UP
}
