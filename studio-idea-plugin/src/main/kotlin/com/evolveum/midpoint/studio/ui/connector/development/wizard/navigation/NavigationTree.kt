/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.navigation

import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep

class NavigationTree {

    val roots: MutableList<NavigationNode> = mutableListOf()

    fun addCategory(
        name: String,
        expanded: Boolean = true
    ): NavigationNode.Category {
        val category = NavigationNode.Category(name = name, expanded = expanded)
        roots += category
        return category
    }

    fun addStep(
        step: ConnectorDevelopmentWizardStep
    ): NavigationNode.Step {
        val node = NavigationNode.Step(step)
        roots += node
        return node
    }

    fun clear() {
        roots.clear()
    }

    fun flattenSteps(): List<ConnectorDevelopmentWizardStep> {

        val result = mutableListOf<ConnectorDevelopmentWizardStep>()

        fun visit(node: NavigationNode) {

            when (node) {
                is NavigationNode.Step -> result += node.step
                is NavigationNode.Category -> node.children.forEach(::visit)
            }
        }

        roots.forEach(::visit)

        return result
    }

    fun flattenVisible(): List<NavigationNode> {

        val result = mutableListOf<NavigationNode>()

        fun visit(node: NavigationNode) {
            result += node

            if (node is NavigationNode.Category && node.expanded) {
                node.children.forEach(::visit)
            }
        }

        roots.forEach(::visit)

        return result
    }

    fun indexOf(step: ConnectorDevelopmentWizardStep): Int = flattenSteps().indexOf(step)

    fun hasVisibleChildren(
        category: NavigationNode.Category
    ): Boolean {

        return category.children.any { child ->

            when (child) {

                is NavigationNode.Step ->
                    child.step.state !=
                            Badge.State.NONE

                is NavigationNode.Category ->
                    hasVisibleChildren(child)
            }
        }
    }

    operator fun get(
        index: Int
    ): ConnectorDevelopmentWizardStep {
        return flattenSteps()[index]
    }

    val size: Int
        get() = flattenSteps().size
}