/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.renderer.prism.type

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.studio.ui.renderer.PrismPropertyDefinitionRenderer
import com.intellij.ui.dsl.builder.Row
import java.io.File

class FileRenderer: PrismPropertyDefinitionRenderer<File> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<File>,
        value: File?
    ) {
        TODO("Not yet implemented")
    }
}