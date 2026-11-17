/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.component

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.IconUtil
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class StatusPanel {

    enum class Status(name: String) {
        // FIXME maybe replace to OperationResultStatusType enum from midpoint
        ERROR("Error"),
        WARNING("Warning"),
        INFO("Info"),
        SUCCESS("Success");

        fun getName(): String = name
    }

    var status: Status? = null
    var text: String? = null

    var elapsedLabel: ElapsedTimerLabel? = null
        private set

    fun showAlertComponent(status: Status?, title: String, description: String?, color: JBColor?): JComponent {
        val panel = JPanel(CardLayout())
        panel.setLayout(VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false))
        panel.setBorder(JBUI.Borders.empty(120, 20))
        panel.setBackground(UIUtil.getPanelBackground())

        val iconWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        iconWrapper.setOpaque(false)
        iconWrapper.add(JBLabel(when (status) {
            Status.ERROR -> AllIcons.General.Error
            Status.SUCCESS -> AllIcons.General.SuccessDialog
            else -> null
        }?.let { IconUtil.scale(it, null, 2.0f) }))
        panel.add(iconWrapper)

        val textLabel = JBLabel(title, SwingConstants.CENTER)
        textLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN))
        textLabel.setForeground(color)
        panel.add(textLabel)

        panel.add(createDescriptionPane(description))

        return panel
    }

    /**
     * Centered, read-only description that wraps. A text pane reports the width of its longest line as preferred
     * width, so the preferred size is computed for a bounded width, otherwise long messages get cut off.
     */
    private fun createDescriptionPane(description: String?): JTextPane {
        val pane = JTextPane()
        pane.isEditable = false
        pane.setOpaque(false)
        pane.setFocusable(false)
        pane.text = description
        pane.setForeground(UIUtil.getContextHelpForeground())
        pane.setBorder(EmptyBorder(JBUI.insets(0, 10)))

        val doc = pane.styledDocument
        val centerAttribute = SimpleAttributeSet()
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER)
        doc.setParagraphAttributes(0, doc.length, centerAttribute, false)

        val width = JBUI.scale(DESCRIPTION_MAX_WIDTH)
        pane.setSize(width, Short.MAX_VALUE.toInt())
        pane.preferredSize = Dimension(width, pane.preferredSize.height)

        return pane
    }

    fun showLoadingComponent(title: String, description: String?, elapsed: Long): JComponent {
        val panel = JPanel(CardLayout())
        panel.setLayout(VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false))
        panel.setBorder(JBUI.Borders.empty(120, 20))
        panel.setBackground(UIUtil.getPanelBackground())

        val iconWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        iconWrapper.setOpaque(false)
        iconWrapper.add(AsyncProcessIcon("Loading"))
        panel.add(iconWrapper)

        val textLabel = JBLabel(title, SwingConstants.CENTER)
        textLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN))
        textLabel.setForeground(JBColor(Color(70, 130, 180), Color(70, 130, 180)))
        panel.add(textLabel)

        panel.add(createDescriptionPane(description))

        elapsedLabel = ElapsedTimerLabel(elapsed)
        elapsedLabel!!.start()
        panel.add(elapsedLabel)

        return panel
    }

    companion object {
        private const val DESCRIPTION_MAX_WIDTH = 760
    }
}
