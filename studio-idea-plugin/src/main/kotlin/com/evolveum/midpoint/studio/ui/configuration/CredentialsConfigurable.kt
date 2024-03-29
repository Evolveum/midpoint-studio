package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.impl.EncryptionService
import com.evolveum.midpoint.studio.impl.configuration.MidPointService
import com.evolveum.midpoint.studio.ui.util.BooleanPropertyPredicate
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ValidationInfoBuilder
import org.apache.commons.lang3.StringUtils
import javax.swing.JLabel

const val NOTIFICATION_KEY = "Credentials change"

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

        val oldPwd = oldPassword.password
        val newPwd = newPassword.password
        val repeatNewPwd = repeatNewPassword.password

        if (newPwd.isNotEmpty() && !newPwd.contentEquals(repeatNewPwd)) {
            MidPointUtils.publishNotification(
                project, NOTIFICATION_KEY, "Change master password",
                "New password and repeat password fields don't match.", NotificationType.ERROR
            )
            return
        }

        val status = EncryptionService.getInstance(project).status

        if (oldPwd.isEmpty() && (status.status == EncryptionService.Status.OK
                    || status.status == EncryptionService.Status.PASSWORD_NOT_SET)
        ) {
            MidPointUtils.publishNotification(
                project, NOTIFICATION_KEY, "Change master password",
                "Can't change credentials password old password is empty.", NotificationType.ERROR
            )
            return
        }

        try {
            EncryptionService.getInstance(project)
                .changeMasterPassword(oldPwd.concatToString(), newPwd.concatToString())

            resetPanel()
        } catch (ex: Exception) {
            MidPointUtils.publishNotification(
                project, NOTIFICATION_KEY, "Change master password",
                "Can't change credentials password, reason: " + ex.message, NotificationType.ERROR
            )
            return
        }
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

        resetPanel()
    }

    private fun resetPanel() {
        oldPassword.text = null
        newPassword.text = null
        repeatNewPassword.text = null

        updateVisibilityAndStatus()
    }

    private fun updateVisibilityAndStatus() {
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
        updateVisibilityAndStatus()

        return panel {
            group(message("CredentialsConfigurable.credentials")) {
                row("Old password:") {
                    cell(oldPassword)
                        .columns(COLUMNS_SHORT)
                        .validationOnApply { validateOldPassword() }
                }
                    .visibleIf(oldVisible)
                row("New password:") {
                    cell(newPassword)
                        .columns(COLUMNS_SHORT)
                        .validationOnInput { validatePasswords() }
                }
                    .visibleIf(newVisible)
                row("Repeat new password:") {
                    cell(repeatNewPassword)
                        .columns(COLUMNS_SHORT)
                        .validationOnInput { validatePasswords() }
                }
                    .visibleIf(newVisible)
            }
            row {
                cell(status)
            }
        }
    }

    private fun validateOldPassword(): ValidationInfo? {
        val password = oldPassword.password

        if (password.isNotEmpty()) {
            val projectId = MidPointService.get(project).settings.projectId
            val currentPwd = MidPointUtils.getPassword(projectId)

            if (StringUtils.isEmpty(currentPwd)) {
                return ValidationInfoBuilder(oldPassword)
                    .error("There is no master password stored in keychain with id '$projectId'. Please set only the new password.")
            }

            if (password.concatToString() != currentPwd) {
                return ValidationInfoBuilder(oldPassword)
                    .error("Old password doesn't match one that is stored in keychain with id '$projectId'.")
            }
        }

        return null
    }

    private fun validatePasswords(): ValidationInfo? {
        val pwd = newPassword.password
        val repeatPwd = repeatNewPassword.password

        if (!pwd.contentEquals(repeatPwd)) {
            return ValidationInfoBuilder(repeatNewPassword)
                .error("New password and repeat password fields don't match.")
        }

        return null
    }
}
