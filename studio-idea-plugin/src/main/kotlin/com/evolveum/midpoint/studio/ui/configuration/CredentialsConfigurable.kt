package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate

/**
 * Created by Viliam Repan (lazyman).
 */
class CredentialsConfigurable : BoundSearchableConfigurable(message("CredentialsConfigurable.title"), "") {

    private var oldVisible = true

    private var newVisible = true

    override fun apply() {
        super.apply()
    }

    override fun isModified(): Boolean {
        return super.isModified() ||
    }

    override fun reset() {

    }

    override fun createPanel(): DialogPanel {
        return panel {
            group(message("CredentialsConfigurable.credentials")) {
                row("Old password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(oldVisible))
                }
                row("New password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(newVisible))
                }
                row("Repeat new password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(newVisible))
                }
            }
        }
    }
}
