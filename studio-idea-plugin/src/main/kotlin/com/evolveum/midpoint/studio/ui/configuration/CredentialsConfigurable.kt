package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

/**
 * Created by Viliam Repan (lazyman).
 */
class CredentialsConfigurable : BoundSearchableConfigurable(message("CredentialsConfigurable.title"), "") {

    override fun createPanel(): DialogPanel {
        return panel {
            group("Environment credentials") {
                row("Path to credentials file:") {
                    textFieldWithBrowseButton()
                        .align(AlignX.FILL)
                }
                row("Old password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("New password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("Repeat new password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
            }
            row {
                checkBox("Separate encrypted properties")
//                    .bindSelected(model::restLogCommunication)
            }
            group("Encrypted properties") {
                row("Path to credentials file:") {
                    textFieldWithBrowseButton()
                        .align(AlignX.FILL)
                }
                row("Old password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("New password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("Repeat new password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
            }
        }
    }
}
