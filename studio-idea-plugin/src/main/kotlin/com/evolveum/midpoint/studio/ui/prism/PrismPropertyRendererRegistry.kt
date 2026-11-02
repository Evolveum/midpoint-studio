/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.studio.ui.PrismPropertyRenderer

class PrismPropertyRendererRegistry(
    private val renderers: List<PrismPropertyRenderer>
) {

    fun findRenderer(
        definition: PrismPropertyDefinition<*>
    ): PrismPropertyRenderer {

        return renderers.firstOrNull {
            it.supports(definition)
        } ?: error(
            "No renderer found for ${definition.itemName}"
        )
    }
}