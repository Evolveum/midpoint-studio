/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Row
import java.time.LocalDateTime

class DateTimeRenderer: PrismPropertyDefinitionRenderer<LocalDateTime> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: LocalDateTime?
    ): () -> LocalDateTime? {
        row.label(propertyDefinition.displayName)
            .align(AlignX.FILL)

        val field = row.textField()
            .align(AlignX.FILL)
            .component

        field.text = value?.toString() ?: ""

        return { field.text.trim().takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) } }
    }
}
