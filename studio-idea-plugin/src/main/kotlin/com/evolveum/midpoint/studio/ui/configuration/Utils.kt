package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

fun <E> validateNotNull(builder: ValidationInfoBuilder, comp: ComboBox<E>): ValidationInfo? {
    return builder.run {
        val value = comp.selectedItem
        when {
            value == null -> error("Please fill in value")
            else -> null
        }
    }
}

fun validateNotBlank(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
    return builder.run {
        val value = textField.text
        when {
            value.isNullOrBlank() -> error("Please fill in value")
            else -> null
        }
    }
}

fun convertObjectTypesListToString(list: List<ObjectTypes>): String {
    if (list.isEmpty()) {
        return ""
    }

    return list.joinToString { it.value }
}

fun convertStringToObjectTypesList(value: String): List<ObjectTypes> {
    if (value.isEmpty()) {
        return emptyList()
    }

    return value
        .split(",")
        .filter { it.isNotBlank() }
        .map(String::trim)
        .mapNotNull { convertToObjectTypes(it) }
        .toList()
}

fun validateTypes(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
    return builder.run {
        val value = textField.text

        val array = value?.split(",") ?: emptyList()
        val result = array.stream()
            .map { it.trim() }
            .filter { convertToObjectTypes(it) == null }
            .toList()

        if (result.isEmpty()) {
            return null
        }

        error("Unknown types: " + result.joinToString { it })
    }
}

fun convertToObjectTypes(item: String): ObjectTypes? {
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