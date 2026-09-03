/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.component

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JLabel

class GenerateConnectorBadge(initialBadge: BadgeType) : JLabel(initialBadge.label ?: "") {

    interface BadgeType {
        val label: String?
        val background: Color?
        val border: Color?
        val foreground: Color?
    }

    @JvmRecord
    data class BadgeStyle(
        val label: String?,
        val background: Color?,
        val border: Color?,
        val foreground: Color?
    )

    enum class State(private val style: BadgeStyle) : BadgeType {
        COMPLETE(
            BadgeStyle(
                "Complete",
                JBColor(Color(0xDDF4E4), Color(0x35523C)),
                JBColor(Color(0x8BC79A), Color(0x5FA86E)),
                JBColor(Color(0x1E4620), Color(0xDDF4E4))
            )
        ),
        IN_PROGRESS(
            BadgeStyle(
                "In progress",
                JBColor(Color(0xD7EEF2), Color(0x2F4F58)),
                JBColor(Color(0x8BC3CC), Color(0x5FA8B5)),
                JBColor(Color(0x0F3B46), Color(0xD7EEF2))
            )
        ),
        EDITED(
            BadgeStyle(
                "Edited",
                JBColor(Color(0xF4E7D6), Color(0x5A4632)),
                JBColor(Color(0xD2B48C), Color(0xB08968)),
                JBColor(Color(0x5C3B12), Color(0xFFEBD2))
            )
        ),
        FIXING(
            BadgeStyle(
                "Fixing",
                JBColor(Color(0xFCE2E2), Color(0x5C3232)),
                JBColor(Color(0xE5A1A1), Color(0xC16B6B)),
                JBColor(Color(0x5A1E1E), Color(0xFFECEC))
            )
        ),
        NONE(
            BadgeStyle(
                "None",
                JBColor(Color(0xECECEC), Color(0x3C3F41)),
                JBColor(Color(0xD0D0D0), Color(0x5A5D5F)),
                JBColor(Color(0x666666), Color(0xBBBBBB))
            )
        );

        override val label: String? get() = style.label
        override val background: Color? get() = style.background
        override val border: Color? get() = style.border
        override val foreground: Color? get() = style.foreground
    }

    enum class Recommended(private val style: BadgeStyle) : BadgeType {
        RECOMMENDED(
            BadgeStyle(
                "Recommended",
                JBColor(Color(0xDDF4E4), Color(0x35523C)),
                JBColor(Color(0x8BC79A), Color(0x5FA86E)),
                JBColor(Color(0x1E4620), Color(0xDDF4E4))
            )
        );

        override val label: String? get() = style.label
        override val background: Color? get() = style.background
        override val border: Color? get() = style.border
        override val foreground: Color? get() = style.foreground
    }

    enum class AiTag(private val style: BadgeStyle) : BadgeType {
        AI_TAG(
            BadgeStyle(
                "AI",
                JBColor(Color(0xDDD0F9), Color(0x4A3A6B)),
                JBColor(Color(0xDDD0F9), Color(0x8A6FD1)),
                JBColor(Color(0x38265C), Color(0xDDD0F9))
            )
        );

        override val label: String? get() = style.label
        override val background: Color? get() = style.background
        override val border: Color? get() = style.border
        override val foreground: Color? get() = style.foreground
    }

    var badge: BadgeType = initialBadge
        private set

    init {
        isOpaque = false
        font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(11f).toFloat())
        border = JBUI.Borders.empty(3, 8)
        foreground = initialBadge.foreground
    }

    fun setBadge(newBadge: BadgeType) {
        this.badge = newBadge
        text = newBadge.label ?: ""
        foreground = newBadge.foreground
        repaint()
    }

    fun setState(state: State) {
        setBadge(state)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            val arc = JBUI.scale(8)

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            )

            badge.background?.let {
                g2.color = it
                g2.fillRoundRect(0, 0, width, height, arc, arc)
            }

            badge.border?.let {
                g2.color = it
                g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc)
            }

            super.paintComponent(g2)
        } finally {
            g2.dispose()
        }
    }
}
