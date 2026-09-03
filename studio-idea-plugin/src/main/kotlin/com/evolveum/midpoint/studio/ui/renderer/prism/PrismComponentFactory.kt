/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer.prism

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import javax.swing.JComponent

class PrismComponentFactory(
    private val rendererRegistry: PrismPropertyRendererRegistry
) {

    fun create(
        row: Row,
        definition: PrismPropertyDefinition<*>,
        value: Any?
    ): Cell<out JComponent> {

        val renderer = rendererRegistry.findRenderer(
            definition
        )

        return renderer.render(
            row,
            definition,
            value
        )
    }
}