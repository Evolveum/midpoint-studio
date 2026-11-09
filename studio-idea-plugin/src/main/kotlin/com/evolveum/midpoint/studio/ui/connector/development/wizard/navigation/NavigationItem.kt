/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.navigation

import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep

sealed class NavigationItem {

    data class Header(
        val node: NavigationNode.Category
    ) : NavigationItem() {

        val name: String
            get() = node.name

        val expanded: Boolean
            get() = node.expanded
    }

    data class Step(
        val node: NavigationNode.Step,
        val wizardStepIndex: Int
    ) : NavigationItem() {

        val step: ConnectorDevelopmentWizardStep
            get() = node.step

        val name: String?
            get() = step.component?.getName()

        val state: Badge.State
            get() = step.state
    }
}