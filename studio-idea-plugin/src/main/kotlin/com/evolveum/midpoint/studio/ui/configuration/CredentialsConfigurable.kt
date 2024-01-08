package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.impl.EncryptionService
import com.evolveum.midpoint.studio.impl.configuration.MidPointService
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import javax.swing.JLabel

/**
 * Created by Viliam Repan (lazyman).
 */
class CredentialsConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("CredentialsConfigurable.title"), "") {

    private var oldPassword: Cell<JBPasswordField>? = null

    private var newPassword: Cell<JBPasswordField>? = null

    private var repeatNewPassword: Cell<JBPasswordField>? = null

    private var text: Cell<JLabel>? = null

    private var oldVisible = true

    private var newVisible = true

    override fun apply() {
        val oldPwd = oldPassword?.component?.password
        val newPwd = newPassword?.component?.password

        if (oldPwd != null && oldPwd.isNotEmpty() && newPwd != null && newPwd.isNotEmpty()) {
            try {
                EncryptionService.getInstance(project)
                    .changeMasterPassword(oldPwd.concatToString(), newPwd.concatToString())
            } catch (ex: Exception) {
                error("Couldn't change master password: ${ex.message}")
            }
        }
    }

    override fun isModified(): Boolean {
        return super.isModified() || isModified(oldPassword) || isModified(newPassword) || isModified(repeatNewPassword)
    }

    private fun isModified(cell: Cell<JBPasswordField>?): Boolean {
        return cell?.component?.password?.isNotEmpty() ?: false
    }

    override fun reset() {
        super.reset()

        oldPassword?.component?.text = null
        newPassword?.component?.text = null
        repeatNewPassword?.component?.text = null
    }

    override fun createPanel(): DialogPanel {
        return panel {
            group(message("CredentialsConfigurable.credentials")) {
                row("Old password:") {
                    oldPassword = cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(oldVisible))
                        .validationOnInput { component ->
                            val password = component.password

                            if (password.isNotEmpty()) {
                                val projectId = MidPointService.get(project).settings.projectId
                                val currentPwd = MidPointUtils.getPassword(projectId)

                                if (password.concatToString() != currentPwd) {
                                    error("Old password doesn't match one that is stored in keychain with id $projectId")
                                }
                            }

                            null
                        }
                }
                row("New password:") {
                    newPassword = cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(newVisible))
                }
                row("Repeat new password:") {
                    repeatNewPassword = cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                        .visibleIf(ComponentPredicate.fromValue(newVisible))
                        .validationOnApply() { component ->
                            val password = newPassword?.component?.password
                            val repeatPassword = component.password

                            if (!repeatPassword.contentEquals(password)) {
                                error("New password and repeat password fields don't match.")
                            } else {
                                null
                            }
                        }
                }
            }
            row {
                text = label("Encryption service is correctly configured.")
            }
        }
    }
}
