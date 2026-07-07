package com.evolveum.midpoint.studio.ui.connector.generator.component

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.components.JBLabel
import com.intellij.util.IconUtil.colorize
import com.intellij.util.IconUtil.scale
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

class AlertPanel(layout: LayoutManager?, private val arcSize: Int, text: String, subtext: String?) : JPanel(layout) {

    enum class Type {
        AI,
        INFO
    }

    init {
        setOpaque(false)
        setBackground(COLOR_BACKGROUND)
        setBorder(RoundedLineBorder(COLOR_BORDER, arcSize, 2))

        val coloredIcon = colorize(AllIcons.Diff.MagicResolve, COLOR_FOREGROUND)
        val aiAlertIcon = JBLabel()
        val largeIcon = scale(coloredIcon, aiAlertIcon, 1.7f)
        aiAlertIcon.icon = largeIcon
        aiAlertIcon.setHorizontalAlignment(SwingConstants.CENTER)
        aiAlertIcon.preferredSize = Dimension(-1, 50)
        aiAlertIcon.setOpaque(false)
        add(aiAlertIcon, BorderLayout.WEST)

        val contentPanel = JPanel(GridLayout(2, 1))
        contentPanel.setOpaque(false)

        val textJBLabel = JBLabel(text)
        textJBLabel.setFont(JBFont.label().asBold().deriveFont(JBUI.scale(14f)))
        textJBLabel.setForeground(COLOR_FOREGROUND)
        textJBLabel.setBorder(EmptyBorder(JBUI.insets(0, 10)))
        contentPanel.add(textJBLabel)

        val subtextJTextPanel = JTextPane()
        subtextJTextPanel.isEditable = false
        subtextJTextPanel.setOpaque(false)
        subtextJTextPanel.setForeground(COLOR_FOREGROUND)
        subtextJTextPanel.setFocusable(false)
        subtextJTextPanel.text = subtext
        subtextJTextPanel.setBorder(EmptyBorder(JBUI.insets(0, 10)))
        contentPanel.add(subtextJTextPanel)

        add(contentPanel, BorderLayout.CENTER)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2.color = getBackground()
        val scaledArc = JBUI.scale(arcSize)

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), scaledArc, scaledArc)
        g2.dispose()
    }

    companion object {
        private val COLOR_BORDER = JBColor(Color(0xDDD0F9), Color(0x8A6FD1))
        private val COLOR_BACKGROUND = JBColor(Color(0xDDD0F9), Color(0x4A3A6B))
        private val COLOR_FOREGROUND = JBColor(Color(0x38265C), Color(0xDDD0F9))
    }
}
