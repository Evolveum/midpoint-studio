/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step;

import com.evolveum.midpoint.xml.ns._public.common.common_3.DataAccessPermissionType;

/**
 * Defines the data access permissions available to a user or system.
 *
 * This enum is the client-facing representation of {@link DataAccessPermissionType}.
 * Both enums must contain the exact same permission values
 */
public enum DataAccessPermission {

    STATISTICS_ACCESS("DataAccessPermission.schema.title",
            "DataAccessPermission.schema.description"),

    SCHEMA_ACCESS("DataAccessPermission.statisticalData.title",
            "DataAccessPermission.statisticalData.description"),

    RAW_DATA_ACCESS("DataAccessPermission.rawData.title",
            "DataAccessPermission.rawData.description");

    private final String title;
    private final String description;

    DataAccessPermission(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public static DataAccessPermission fromType(DataAccessPermissionType type) {
        return switch (type) {
            case STATISTICS_ACCESS -> STATISTICS_ACCESS;
            case SCHEMA_ACCESS -> SCHEMA_ACCESS;
            case RAW_DATA_ACCESS -> RAW_DATA_ACCESS;
        };
    }
}