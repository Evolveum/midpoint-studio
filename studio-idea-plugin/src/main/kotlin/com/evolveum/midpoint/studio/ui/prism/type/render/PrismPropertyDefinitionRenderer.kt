/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.Row

/**
 * Renders an input for a property definition into a row and returns a supplier of the value currently entered.
 */
interface PrismPropertyDefinitionRenderer<T> {

    fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: T?
    ): () -> T?
}
