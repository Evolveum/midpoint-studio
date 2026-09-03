/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.component

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Timer

class ElapsedTimerLabel(private var elapsedSeconds: Long) : JBLabel("", CENTER) {
    private val timer: Timer

    init {
        setForeground(UIUtil.getInactiveTextColor())
        updateText()

        timer = Timer(1000, ActionListener { e: ActionEvent? ->
            elapsedSeconds++
            updateText()
        })
    }

    fun start() {
        timer.start()
    }

    fun stop() {
        timer.stop()
    }

    private fun updateText() {
        text = FORMAT.format(
            elapsedSeconds / 60,
            elapsedSeconds % 60
        )
    }

    companion object {
        private const val FORMAT = "Elapsed time: %dm %ds"
    }
}
