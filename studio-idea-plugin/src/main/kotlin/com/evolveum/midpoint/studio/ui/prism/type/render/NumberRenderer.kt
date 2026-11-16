/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.prism.type.render

import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Row
import java.math.BigDecimal
import java.math.BigInteger

class NumberRenderer: PrismPropertyDefinitionRenderer<Any> {

    override fun render(
        row: Row,
        propertyDefinition: PrismPropertyDefinition<*>,
        value: Any?
    ): () -> Any? {
        row.label(propertyDefinition.displayName)
            .align(AlignX.FILL)

        val field = row.intTextField()
            .align(AlignX.FILL)
            .component

        field.text = value?.toString() ?: ""

        return { convert(field.text.trim(), propertyDefinition.typeClass) }
    }

    private fun convert(text: String, type: Class<*>?): Any? {
        if (text.isEmpty()) {
            return null
        }

        return when (type) {
            Int::class.javaObjectType, Int::class.javaPrimitiveType -> text.toInt()
            Short::class.javaObjectType, Short::class.javaPrimitiveType -> text.toShort()
            Byte::class.javaObjectType, Byte::class.javaPrimitiveType -> text.toByte()
            Double::class.javaObjectType, Double::class.javaPrimitiveType -> text.toDouble()
            Float::class.javaObjectType, Float::class.javaPrimitiveType -> text.toFloat()
            BigInteger::class.java -> BigInteger(text)
            BigDecimal::class.java -> BigDecimal(text)
            else -> text.toLong()
        }
    }
}
