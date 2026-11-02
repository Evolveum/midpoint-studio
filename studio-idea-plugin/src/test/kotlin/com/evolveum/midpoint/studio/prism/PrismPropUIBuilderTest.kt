/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.prism

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.ui.PrismPropertyUIBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PrismPropUIBuilderTest: BasePlatformTestCase() {


    fun testPropertiesUIBuild() {

        val items = listOf(
            ItemName("name"),
            ItemName("displayName")
        )

        val builder = PrismPropertyUIBuilder()
        builder.build(items)
    }
}