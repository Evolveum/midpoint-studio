package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step;

import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class LoadingStep extends StepAdapter {

    private final AsyncProcessIcon loadingIcon = new AsyncProcessIcon("Loading");
    private final JBPanel<?> panel = new JBPanel<>();
    private final String title;
    private final String description;
    private final String elapsedTime;

    public LoadingStep(String title, String description, String elapsedTime) {
        this.title = title;
        this.description = description;
        this.elapsedTime = elapsedTime;
    }

    @Override
    public void _init() {
        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        panel.setBorder(JBUI.Borders.empty(120, 20));
        panel.setBackground(UIUtil.getPanelBackground());

        AsyncProcessIcon progressIcon = new AsyncProcessIcon("");
        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(progressIcon);
        panel.add(iconWrapper);

        JBLabel titleLabel = new JBLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN));
        titleLabel.setForeground(new JBColor(new Color(70, 130, 180), new Color(70, 130, 180)));
        panel.add(titleLabel);

        JBLabel descriptionLabel = new JBLabel(description, SwingConstants.CENTER);
        descriptionLabel.setForeground(UIUtil.getContextHelpForeground());
        panel.add(descriptionLabel);

        JBLabel timerLabel = new JBLabel("Elapsed time: " + elapsedTime, SwingConstants.CENTER);
        timerLabel.setForeground(UIUtil.getInactiveTextColor());
        panel.add(timerLabel);
    }
}
