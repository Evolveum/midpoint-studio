package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel

/**
 * Created by Viliam Repan (lazyman).
 */
class OtherConfigurable : BoundSearchableConfigurable(message("OtherConfigurable.title"), "") {

    override fun createPanel(): DialogPanel {
        return panel {
            row() {
                checkBox("Ignore missing properties")
                    .comment("E.g. don't fail to upload object if not all properties have been resolved and replaced")
            }
            group("Experimental") {
                row() {
                    checkBox("Enable Axiom Query")
                        .comment("Enable plugin support for Axiom Query language")
                }
                row() {
                    checkBox("Enable MidPoint Studio Properties")
                        .comment("Enable plugin support for MidPoint Studio Properties language")
                }
            }
        }
    }
}
