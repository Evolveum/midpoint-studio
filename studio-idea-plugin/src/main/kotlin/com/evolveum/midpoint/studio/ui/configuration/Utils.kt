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
        .split("[,\\s")
        .mapNotNull { convertToObjectTypes(it) }
        .toList() as List<ObjectTypes>
}

fun validateTypes(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
    return builder.run {
        val value = textField.text

        val array = value?.split("[,\\s]") ?: emptyList()
        when {
            value.isNullOrBlank() -> null
            array.map { convertToObjectTypes(it) }.none { it == null } -> null
            else -> error("Unknown types: " + array.filter { convertToObjectTypes(it) != null }.toList())
        }
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