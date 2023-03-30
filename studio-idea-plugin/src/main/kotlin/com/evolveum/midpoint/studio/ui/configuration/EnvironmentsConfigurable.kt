package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel

/**
 * Created by Viliam Repan (lazyman).
 */
class EnvironmentsConfigurable : BoundSearchableConfigurable(message("EnvironmentsConfigurable.title"), "") {

    override fun createPanel(): DialogPanel {
        return panel {
            group("EnvironmentsConfigurable") {
                row("MidPoint module:") {
                }.comment("Select module which will have Midpoint Facet to enable Midpoint Studio functionality")
            }
        }
    }
}
