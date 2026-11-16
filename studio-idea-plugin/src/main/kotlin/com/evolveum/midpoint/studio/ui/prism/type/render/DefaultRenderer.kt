/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Row

/**
 * Fallback for types without a dedicated renderer, the entered text is returned as is.
 */
class DefaultRenderer: PrismPropertyDefinitionRenderer<Any> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any?
    ): () -> Any? {
        row.label(propertyDefinition.displayName)
            .align(AlignX.FILL)

        val field = row.textField()
            .align(AlignX.FILL)
            .component

        field.text = value?.toString() ?: ""

        return { field.text.takeIf { it.isNotBlank() } }
    }
}
