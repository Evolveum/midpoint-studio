/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.step

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Align
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager

class ConnectorDevelopmentDisabledStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "",
        ) {
            row {
                cell(createHeader())
                    .align(Align.FILL)
            }

            row {
                cell(
                    createInfoCard(
                        icon = UIManager.getIcon("OptionPane.informationIcon"),
                        title = "Recommendation",
                        paragraphs = listOf(
                            "Enable this feature only in dedicated midPoint " +
                                    "instances used for development and testing, not in production.",
                            "Develop and test new integrations in a non-production " +
                                    "environment, then deploy them to production through a controlled process."
                        )
                    )
                ).align(Align.FILL)
            }

            row {
                cell(
                    createUsageCard()
                ).align(Align.FILL)
            }
        }
    }

    override fun _init() {
        super._init()
    }

    private fun createHeader(): JPanel {
        val icon = JLabel(UIManager.getIcon("OptionPane.warningIcon")).apply {
            border = JBUI.Borders.empty(8)
            verticalAlignment = JLabel.TOP
        }

        val text = JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            isOpaque = false

            add(JLabel("Connector generator is disabled").apply {
                font = font.deriveFont(Font.BOLD, 24f)
                alignmentX = Component.LEFT_ALIGNMENT
            })

            add(JLabel(
                "Connector generator is disabled by default to mitigate potential security risks."
            ).apply {
                border = JBUI.Borders.emptyTop(12)
                alignmentX = Component.LEFT_ALIGNMENT
            })

            add(JLabel(
                "<html>Because Connector generator allows arbitrary code execution by design, " +
                        "even authorized users can potentially modify or damage the system " +
                        "on which midPoint runs.</html>"
            ).apply {
                border = JBUI.Borders.emptyTop(8)
                alignmentX = Component.LEFT_ALIGNMENT
            })
        }

        return JPanel(BorderLayout(24, 0)).apply {
            isOpaque = false
            add(icon, BorderLayout.WEST)
            add(text, BorderLayout.CENTER)
            border = JBUI.Borders.emptyBottom(28)
        }
    }

    private fun createInfoCard(
        icon: Icon?,
        title: String,
        paragraphs: List<String>
    ): JPanel {
        val content = JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            isOpaque = false

            add(JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
                isOpaque = false
                add(JLabel(icon))
                add(JLabel(title).apply {
                    font = font.deriveFont(Font.BOLD, 16f)
                })
                alignmentX = Component.LEFT_ALIGNMENT
            })

            paragraphs.forEach { paragraph ->
                add(JLabel("<html>${paragraph}</html>").apply {
                    border = JBUI.Borders.emptyTop(12)
                    alignmentX = Component.LEFT_ALIGNMENT
                })
            }
        }

        return JPanel(BorderLayout()).apply {
            add(content, BorderLayout.CENTER)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    JBColor.border()
                ),
                JBUI.Borders.empty(16)
            )
            isOpaque = true
            background = UIManager.getColor("Panel.background")
        }
    }

    private fun createUsageCard(): JPanel {
        val content = JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            isOpaque = false

            add(JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
                isOpaque = false
                add(JLabel(UIManager.getIcon("OptionPane.questionIcon")))
                add(JLabel("How can I use it?").apply {
                    font = font.deriveFont(Font.BOLD, 16f)
                })
                alignmentX = Component.LEFT_ALIGNMENT
            })

            add(JLabel(
                "If this is a production instance, switch to (or install) a development or testing instance."
            ).apply {
                border = JBUI.Borders.emptyTop(12)
                alignmentX = Component.LEFT_ALIGNMENT
            })

            add(docButton(
                "MidPoint installation documentation",
                "https://docs.evolveum.com/midpoint/"
            ))

            add(JLabel(
                "If this is a non-production instance, you can enable the connector generator here."
            ).apply {
                border = JBUI.Borders.emptyTop(22)
                alignmentX = Component.LEFT_ALIGNMENT
            })

            add(docButton(
                "Connector generator configuration documentation",
                "https://docs.evolveum.com/"
            ))
        }

        return JPanel(BorderLayout()).apply {
            add(content, BorderLayout.CENTER)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    JBColor.border()
                ),
                JBUI.Borders.empty(16)
            )
            background = UIManager.getColor("Panel.background")
        }
    }

    private fun docButton(text: String, url: String): JButton =
        JButton(text).apply {
            isFocusable = true
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener {
                com.intellij.ide.BrowserUtil.browse(url)
            }
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    JBColor.border()
                ),
                JBUI.Borders.empty(6, 10)
            )
            isContentAreaFilled = false
        }
}