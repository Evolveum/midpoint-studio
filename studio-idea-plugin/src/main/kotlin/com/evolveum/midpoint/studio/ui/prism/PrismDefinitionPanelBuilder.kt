/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism

import com.evolveum.midpoint.prism.ItemDefinition
import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.prism.PrismReferenceDefinition
import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.ui.prism.type.SupportedType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.util.Collections
import java.util.IdentityHashMap
import javax.activation.UnsupportedDataTypeException
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

/**
 * Builds a dialog panel from a Prism definition.
 *
 * Recursively processes item definitions and renders properties, containers, and references using their corresponding
 * renderers.
 */
class PrismDefinitionPanelBuilder {

    /**
     * Builds a dialog panel for the given Prism container definition.
     *
     * @param definition Root container definition used to build the panel.
     * @return The generated dialog panel.
     */
    fun build(
        definition: PrismContainerDefinition<*>
    ): DialogPanel {

        return panel {
            val visited = Collections.newSetFromMap(
                IdentityHashMap<PrismContainerDefinition<*>, Boolean>()
            )

            renderItemDefinition(
                definition = definition,
                visited = visited
            )
        }
    }

    /**
     * Builds a dialog panel containing only the direct child definitions whose item names match the supplied names,
     * ignoring namespaces.
     *
     * Each matching definition is rendered according to its type, including ts nested definitions when applicable.
     * Circular container references are detected and handled to prevent infinite recursion.
     *
     * @param itemNames Names of the item definitions to include in the panel.
     * Namespace information is ignored when comparing names.
     * @param definition Root container definition whose direct child definitions are filtered and rendered.
     * @return The generated dialog panel containing the matching definitions.
     */
    fun build(
        itemNames: List<ItemName>,
        definition: PrismContainerDefinition<*>
    ): DialogPanel {

        return panel {
            val visited = Collections.newSetFromMap(
                IdentityHashMap<PrismContainerDefinition<*>, Boolean>()
            )

            definition.definitions
                .filter { itemNames.contains(it.itemName.withoutNamespace()) }
                .forEach { itemDefinition ->
                    renderItemDefinition(
                        definition = itemDefinition,
                        visited = visited
                    )
                }
        }
    }

    /**
     * Renders an item definition according to its type.
     *
     * Properties, containers, and references are rendered using their corresponding rendering methods.
     * Unsupported definition types are displayed with an informational label.
     *
     * @param definition Item definition to render.
     * @param visited Set of container definitions currently being processed, used to detect circular references.
     * @throws UnsupportedDataTypeException If the property data type is not supported.
     */
    @Throws(UnsupportedDataTypeException::class)
    private fun Panel.renderItemDefinition(
        definition: ItemDefinition<*>,
        visited: MutableSet<PrismContainerDefinition<*>>
    ) {
        when (definition) {

            is PrismPropertyDefinition<*> -> {
                row {
                    renderPropertyDefinition(this, definition)
                }
            }

            is PrismContainerDefinition<*> -> {
                group(definition.displayName) {
                    renderContainerDefinition(definition, visited)
                }
            }

            is PrismReferenceDefinition -> {
                row {
                    renderReferenceDefinition(this, definition)
                }
            }

            else -> {
                row(definition.displayName) {
                    label("Unsupported: ${definition.javaClass.simpleName}")
                }
            }
        }
    }

    @Throws(UnsupportedDataTypeException::class)
    private fun Panel.renderContainerDefinition(
        definition: PrismContainerDefinition<*>,
        visited: MutableSet<PrismContainerDefinition<*>>
    ) {
        if (!visited.add(definition)) {
            row {
                label(definition.displayName)
            }
            return
        }

        try {
            definition.definitions.forEach { child ->
                renderItemDefinition(child, visited)
            }
        } finally {
            visited.remove(definition)
        }
    }

    @Throws(UnsupportedDataTypeException::class)
    private fun renderPropertyDefinition(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>
    ) {
        val connectorConfigurationType = SupportedType.ConnectorConfiguration.getConnectorConfiguration(
            propertyDefinition.typeClass
        )
        val renderer = connectorConfigurationType.renderer()

        renderer.render(
            row,
            propertyDefinition,
            null
        )
    }

    private fun renderReferenceDefinition(
        row: Row,
        definition: PrismReferenceDefinition
    ) {
        row.label("Reference: ${definition.displayName}")
    }
}
