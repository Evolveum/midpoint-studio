/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.Row

interface PrismPropertyDefinitionRenderer<T> {

    fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<T>,
        value: T?
    )
}
