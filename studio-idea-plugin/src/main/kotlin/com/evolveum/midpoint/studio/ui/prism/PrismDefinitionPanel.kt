/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism

import com.evolveum.midpoint.prism.path.ItemName
import com.intellij.openapi.ui.DialogPanel

/**
 * Panel rendered from prism definitions together with the values currently entered in its fields.
 */
class PrismDefinitionPanel(
    val panel: DialogPanel,
    private val suppliers: Map<ItemName, () -> Any?>
) {

    /**
     * Current field values keyed by the item name of the rendered property definition.
     */
    fun values(): Map<ItemName, Any?> = suppliers.mapValues { it.value() }
}
