package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class LoadPanel extends JBPanel<LoadPanel> {

    private final JBLabel textLabel;
    private final JBLabel subTextLabel;
    private final JBLabel elapsed;

    public LoadPanel(String title, String description, long elapsed) {
        this.setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        this.setBorder(JBUI.Borders.empty(120, 20));
        this.setBackground(UIUtil.getPanelBackground());

        AsyncProcessIcon progressIcon = new AsyncProcessIcon("Loading");
        JBPanel<?> iconWrapper = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(progressIcon);
        this.add(iconWrapper);

        textLabel = new JBLabel(title, SwingConstants.CENTER);
        textLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN));
        textLabel.setForeground(new JBColor(new Color(70, 130, 180), new Color(70, 130, 180)));
        this.add(textLabel);

        subTextLabel = new JBLabel(description, SwingConstants.CENTER);
        subTextLabel.setForeground(UIUtil.getContextHelpForeground());
        this.add(subTextLabel);

        this.elapsed = new JBLabel("Elapsed time: %dm %ds".formatted(elapsed / 60, elapsed % 60), SwingConstants.CENTER);
        this.elapsed.setForeground(UIUtil.getInactiveTextColor());
        this.add(this.elapsed);
    }

    public void setTitle(String title) {
        textLabel.setText(title);
    }

    public void setDescription(String description) {
        subTextLabel.setText(description);
    }

    public void setElapsed(long elapsed) {
        this.elapsed.setText("Elapsed time: %dm %ds".formatted(elapsed / 60, elapsed % 60));
    }
}
