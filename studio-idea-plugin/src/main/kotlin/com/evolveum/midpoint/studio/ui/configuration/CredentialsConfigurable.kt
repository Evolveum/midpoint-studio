package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.impl.EncryptionService
import com.evolveum.midpoint.studio.impl.configuration.MidPointService
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.ValidationInfoBuilder
import org.apache.commons.lang3.StringUtils
import javax.swing.JLabel

/**
 * Created by Viliam Repan (lazyman).
 */
class CredentialsConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("CredentialsConfigurable.title"), "") {

    private val oldPassword = JBPasswordField()

    private val newPassword = JBPasswordField()

    private val repeatNewPassword = JBPasswordField()

    private val status = JLabel()

    private val oldVisible = BooleanPropertyPredicate(true)

    private val newVisible = BooleanPropertyPredicate(true)

    override fun apply() {
        super.apply()
        println(">>>> apply")
//        val oldPwd = oldPassword.password
//        val newPwd = newPassword.password
//
//        if (oldPwd != null && oldPwd.isNotEmpty() && newPwd != null && newPwd.isNotEmpty()) {
//            try {
//                EncryptionService.getInstance(project)
//                    .changeMasterPassword(oldPwd.concatToString(), newPwd.concatToString())
//            } catch (ex: Exception) {
//                error("Couldn't change master password: ${ex.message}")
//            }
//        }
    }

    override fun isModified(): Boolean {
        return super.isModified()
                || isModified(oldPassword)
                || isModified(newPassword)
                || isModified(repeatNewPassword)
    }

    private fun isModified(component: JBPasswordField): Boolean {
        return component.password.isNotEmpty()
    }

    override fun reset() {
        super.reset()

        oldPassword.text = null
        newPassword.text = null
        repeatNewPassword.text = null

        val status = EncryptionService.getInstance(project).status

        this.status.text = status.message

        val oldVisible = status.status == EncryptionService.Status.OK
                || status.status == EncryptionService.Status.PASSWORD_NOT_SET
                || status.status == EncryptionService.Status.PASSWORD_INCORRECT
        this.oldVisible.set(oldVisible)

        val newVisible = status.status == EncryptionService.Status.OK
                || status.status == EncryptionService.Status.MISSING_FILE
        this.newVisible.set(newVisible)
    }

    override fun createPanel(): DialogPanel {
        return panel {
            group(message("CredentialsConfigurable.credentials")) {
                row("Old password:") {
                    cell(oldPassword)
                        .columns(COLUMNS_SHORT)
                        .visibleIf(oldVisible)
                        .validationOnApply { validateOldPassword(it) }
                }
                row("New password:") {
                    cell(newPassword)
                        .columns(COLUMNS_SHORT)
                        .visibleIf(newVisible)
                }
                row("Repeat new password:") {
                    cell(repeatNewPassword)
                        .columns(COLUMNS_SHORT)
                        .visibleIf(newVisible)
                        .validationOnApply(::validatePasswords)
                }
            }
            row {
                cell(status)
            }
        }
    }

    private fun validateOldPassword(component: JBPasswordField): ValidationInfo? {
        val password = component.password

        if (password.isNotEmpty()) {
            val projectId = MidPointService.get(project).settings.projectId
            val currentPwd = MidPointUtils.getPassword(projectId)

            if (StringUtils.isEmpty(currentPwd)) {
                return ValidationInfoBuilder(component)
                    .error("There is no master password stored in keychain with id '$projectId'. Please set only the new password.")
            }

            if (password.concatToString() != currentPwd) {
                return ValidationInfoBuilder(component)
                    .error("Old password doesn't match one that is stored in keychain with id '$projectId'.")
            }
        }

        return null
    }

    private fun validatePasswords(
        builder: ValidationInfoBuilder, password: JBPasswordField
    ): ValidationInfo? {
        return builder.run {
            val pwd = newPassword.password
            val repeatPwd = repeatNewPassword.password

            if (!pwd.contentEquals(repeatPwd)) {
                return builder.error("New password and repeat password fields don't match.")
            }

            return null
        }
    }

    class BooleanPropertyPredicate(value: Boolean) : ComponentPredicate() {

        private val property: AtomicBooleanProperty = AtomicBooleanProperty(value)

        override fun addListener(listener: (Boolean) -> Unit) {
        }

        override fun invoke(): Boolean {
            return get()
        }

        fun set(value: Boolean) {
            property.set(value)
        }

        fun get(): Boolean {
            return property.get()
        }
    }
}
