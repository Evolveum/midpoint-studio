package com.evolveum.midpoint.studio.ui.util

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.ui.layout.ComponentPredicate

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