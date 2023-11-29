package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.MidPointService
import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel

/**
 * Created by Viliam Repan (lazyman).
 */
open class MidPointConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("MidPointConfigurable.title"), "") {

    private val panel: MidpointConfigurationEditorPanel

    init {
        val service = MidPointService.getInstance(project)
        val settings = service.settings.copy()

        // todo get proper configuration instead of settings
        panel = MidpointConfigurationEditorPanel(MidPointConfiguration())
    }

    override fun apply() {
        // todo implement
    }

    override fun isModified(): Boolean {
        // todo implement
        return false
    }

    override fun reset() {
        // todo implement
    }

    override fun createPanel(): DialogPanel {
        return panel.createComponent()
    }
}
