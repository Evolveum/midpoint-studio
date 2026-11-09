/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.Row
import java.time.LocalDateTime

class DateTimeRenderer: PrismPropertyDefinitionRenderer<LocalDateTime> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: LocalDateTime?
    ) {
        TODO("Not yet implemented")
    }
}