package com.evolveum.midpoint.studio.ui.configuration

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

fun validateNotBlank(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
    return builder.run {
        val value = textField.text
        when {
            value.isNullOrBlank() -> error("Please fill in value")
            else -> null
        }
    }
}