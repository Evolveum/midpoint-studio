/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer.prism

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.prism.polystring.PolyString
import com.evolveum.midpoint.studio.ui.renderer.PrismPropertyDefinitionRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.BooleanRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.DateTimeRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.DefaultRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.FileRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.NumberRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.PolyStringRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.ProtectedStringRenderer
import com.evolveum.midpoint.studio.ui.renderer.prism.type.StringRenderer
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
        private val componentFactory: () -> PrismPropertyDefinitionRenderer<*>
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

        val javaType: Class<*> = clazz

        fun createRenderer(): PrismPropertyDefinitionRenderer<*> =
            componentFactory()

        companion object {

            fun from(
                definition: PrismPropertyDefinition<*>
            ): ConnectorConfiguration? {
                return entries.firstOrNull {
                    it.javaType == definition.typeClass
                }
            }
        }
    }
}