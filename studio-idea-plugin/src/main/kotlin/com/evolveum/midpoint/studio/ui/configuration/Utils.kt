package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.schema.constants.ObjectTypes
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

fun translateTypesToString(list: List<ObjectTypes>): String {
    if (list.isEmpty()) {
        return ""
    }

    return list.joinToString { it.value }
}

fun translateStringToTypes(value: String): List<ObjectTypes> {
    if (value.isEmpty()) {
        return emptyList()
    }

    return value.split("[,\\s]").mapNotNull { translateToObjectType(it) }
        .toList() as List<ObjectTypes>
}

fun validateTypes(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
    return builder.run {
        val value = textField.text

        val array = value?.split("[,\\s]") ?: emptyList()
        when {
            value.isNullOrBlank() -> null
            array.map { translateToObjectType(it) }.none { it == null } -> null
            else -> error("Unknown types: " + array.filter { translateToObjectType(it) != null }.toList())
        }
    }
}

fun translateToObjectType(item: String): ObjectTypes? {
    if (item.isBlank()) {
        return null
    }

    for (type in ObjectTypes.values()) {
        if (type.value.equals(item)) {
            return type
        }
    }

    return null
}