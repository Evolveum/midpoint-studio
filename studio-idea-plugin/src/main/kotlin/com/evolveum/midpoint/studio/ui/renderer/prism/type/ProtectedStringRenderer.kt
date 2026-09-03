/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer.prism.type

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.studio.ui.renderer.PrismPropertyDefinitionRenderer
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row

class ProtectedStringRenderer: PrismPropertyDefinitionRenderer<ProtectedStringType> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<ProtectedStringType>,
        value: ProtectedStringType?
    ) {
        row.passwordField()
            .align(AlignX.FILL)
            .apply {
                component.text = value?.clearValue ?: ""
            }
    }
}