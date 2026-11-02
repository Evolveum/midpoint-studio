/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.studio.ui.PrismPropertyDefinitionRenderer
import com.intellij.ui.dsl.builder.Row

class BooleanRenderer: PrismPropertyDefinitionRenderer<Boolean> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<Boolean>,
        value: Boolean?
    ) {
        row.checkBox("")
            .apply {
                component.isSelected = value ?: false
            }
    }
}