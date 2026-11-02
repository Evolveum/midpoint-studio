/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type

import com.evolveum.midpoint.studio.ui.PrismPropertyDefinitionRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.BooleanRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.DateTimeRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.DefaultRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.FileRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.NumberRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.PolyStringRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.ProtectedStringRenderer
import com.evolveum.midpoint.studio.ui.prism.type.render.StringRenderer
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.Date

object SupportedType {

    /**
     * Supported types for connector configuration properties.
     */
    enum class ConnectorConfiguration(
        clazz: Class<*>,
        rendererFactory: () -> PrismPropertyDefinitionRenderer<*>
    ) {
        // Basic scalar types
        STRING(
            String::class.java,
            { StringRenderer() }
        ),

        CHARACTER(
            Character::class.java,
            { StringRenderer() }
        ),

        LONG(
            Long::class.java,
            { NumberRenderer() }
        ),

        DOUBLE(
            Double::class.java,
            { NumberRenderer() }
        ),

        FLOAT(
            Float::class.java,
            { NumberRenderer() }
        ),

        INTEGER(
            Integer::class.java,
            { NumberRenderer() }
        ),

        BOOLEAN(
            Boolean::class.java,
            { BooleanRenderer() }
        ),

        // MidPoint-specific types
        PROTECTED_STRING(
            ProtectedStringType::class.java,
            { ProtectedStringRenderer() }
        ),

        POLYSTRING(
            PolyStringType::class.java,
            { PolyStringRenderer() }
        ),

        // Java types commonly represented in connector schemas
        URI(
            java.net.URI::class.java,
            { StringRenderer() }
        ),

        URL(
            java.net.URL::class.java,
            { StringRenderer() }
        ),

        FILE(
            File::class.java,
            { FileRenderer() }
        ),

        // Date/time
        DATE(
            Date::class.java,
            { DateTimeRenderer() }
        ),

        INSTANT(
            Instant::class.java,
            { DateTimeRenderer() }
        ),

        LOCAL_DATE(
            LocalDate::class.java,
            { DateTimeRenderer() }
        ),

        LOCAL_DATE_TIME(
            LocalDateTime::class.java,
            { DateTimeRenderer() }
        ),

        OFFSET_DATE_TIME(
            OffsetDateTime::class.java,
            { DateTimeRenderer() }
        ),

        ZONED_DATE_TIME(
            ZonedDateTime::class.java,
            { DateTimeRenderer() }
        ),

        // Generic / unknown
        ENUM(
            Any::class.java,
            { DefaultRenderer() }
        ),

        OBJECT(
            Any::class.java,
            { DefaultRenderer() }
        );

        companion object {
            fun getConnectorConfiguration(clazz: Class<*>): ConnectorConfiguration {
                return when {
                    clazz.isEnum -> ENUM
                    else -> entries.firstOrNull {
                        it != ENUM && it != OBJECT && it.javaClass == clazz
                    } ?: OBJECT
                }
            }
        }
    }
}