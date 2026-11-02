/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismContext
import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.prism.PrismReferenceDefinition
import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.ui.prism.type.SupportedType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel

class PrismPropertyUIBuilder {

    fun build(
        items: List<ItemName>
    ): DialogPanel {

        val definitions = PrismContext.get()
            .schemaRegistry
            .findItemDefinitionsByElementName(ItemName("displayName"))

        definitions.forEach { definition ->
            when (definition) {
                is PrismPropertyDefinition<*> -> {

                    val connectorConfigurationType = SupportedType.ConnectorConfiguration.getConnectorConfiguration(
                        definition.typeClass
                    )

                    println("Def: $definition - Property: ${connectorConfigurationType.javaClass}")
                }

                is PrismContainerDefinition<*> -> {
                    println("Container: ${definition.typeName}")
                }

                is PrismReferenceDefinition -> {
                    println("Reference: ${definition.typeName}")
                }
            }
        }

        return panel {}
    }

//    fun build(
//        definition: PrismContainerDefinition<*>,
//        items: List<ItemName>
//    ) {
//        // TODO impl
//    }
//
//    fun build(
//        definition: PrismReferenceDefinition,
//        items: List<ItemName>
//    ) {
//        // TODO impl
//    }
//
//    fun build(
//        definition: PrismPropertyDefinition<*>,
//        items: List<ItemName>
//    ) {
//        // TODO impl
//    }
}
