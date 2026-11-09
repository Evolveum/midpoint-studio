/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Row

class NumberRenderer: PrismPropertyDefinitionRenderer<Any> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any?
    ) {
        row.label(propertyDefinition.displayName)
            .align(AlignX.FILL)

        row.intTextField()
            .align(AlignX.FILL)
            .apply {
                component.text = value?.toString() ?: ""
            }
    }
}