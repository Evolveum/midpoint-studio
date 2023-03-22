package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.impl.MidPointService
import com.evolveum.midpoint.studio.impl.MidPointSettings
import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

/**
 * Created by Viliam Repan (lazyman).
 */
class OtherConfigurable(val project: Project) : BoundSearchableConfigurable(message("OtherConfigurable.title"), "") {

    var model: MidPointSettings

    init {
        model = MidPointService.getInstance(project).settings.copy()
    }

    override fun apply() {
        val modified = isModified

        super.apply()

        if (modified) {
            val ms = MidPointService.getInstance(project)
            ms.settings = model.copy()
        }
    }

    override fun createPanel(): DialogPanel {
        return panel {
            row() {
                checkBox(message("OtherConfigurable.ignoreMissingKeys"))
                    .bindSelected(
                        { model.isIgnoreMissingKeys },
                        { model.isIgnoreMissingKeys = it })
                    .comment(message("OtherConfigurable.ignoreMissingKeys.comment"))
            }
        }
    }
}
