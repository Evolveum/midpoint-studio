/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.navigation

import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep

sealed class NavigationNode {

    class Category(
        val name: String,
        var expanded: Boolean = true
    ) : NavigationNode() {

        val children:
                MutableList<NavigationNode> =
            mutableListOf()

        fun addStep(
            step: ConnectorGeneratorGeneralWizardStep
        ): Step {
            val node =
                Step(step)

            children += node

            return node
        }

        fun addCategory(
            name: String,
            expanded: Boolean = true
        ): Category {

            val category =
                Category(
                    name = name,
                    expanded = expanded
                )

            children += category

            return category
        }
    }

    class Step(
        val step: ConnectorGeneratorGeneralWizardStep
    ) : NavigationNode()
}