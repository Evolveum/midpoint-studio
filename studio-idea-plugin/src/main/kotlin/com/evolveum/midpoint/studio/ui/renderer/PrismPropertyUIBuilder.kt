/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer

import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.ui.renderer.prism.SupportedType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel

class PrismPropertyUIBuilder {

    fun build(
        definition: PrismContainerDefinition<*>,
        values: Map<ItemName, Any?> = emptyMap()
    ): DialogPanel {
        return panel {
            definition.definitions
                .filterIsInstance<PrismPropertyDefinition<*>>()
                .forEach { propertyDefinition ->
                    buildProperty(
                        row = this,
                        propertyDefinition = propertyDefinition,
                        value = values[propertyDefinition.itemName]
                    )
                }
        }
    }

    fun buildProperty(
        row: Panel,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any? = null
    ) {
        val label =
            propertyDefinition.displayName
                ?: propertyDefinition.itemName.localPart

        row.row(label) {
            renderProperty(
                row = this,
                propertyDefinition = propertyDefinition,
                value = value
            )
        }
    }

    private fun renderProperty(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any?
    ) {
        val configurationType =
            SupportedType.ConnectorConfiguration.from(propertyDefinition)
                ?: error(
                    "Unsupported connector configuration type: " +
                            "${propertyDefinition.typeClass} " +
                            "for property ${propertyDefinition.itemName}"
                )

        val renderer = configurationType.createRenderer()

        render(
            renderer = renderer,
            row = row,
            propertyDefinition = propertyDefinition,
            value = value
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun render(
        renderer: PrismPropertyDefinitionRenderer<*>,
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any?
    ) {
        (renderer as PrismPropertyDefinitionRenderer<Any?>)
            .render(
                row,
                propertyDefinition as PrismPropertyDefinition<Any?>,
                value
            )
    }
}
