package com.evolveum.midpoint.studio.ui.metrics;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class NodesPanel extends JPanel {

    public NodesPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initLayout();
    }

    private void initLayout() {
        add(new NodePanel(MidPointUtils.generateAwtColor(), "https://demo1.evolveum.com/midpoint"));
        add(new NodePanel(MidPointUtils.generateAwtColor(), "https://demo2.evolveum.com/midpoint"));
        add(new NodePanel(MidPointUtils.generateAwtColor(), "https://demo3.evolveum.com/midpoint"));
    }

    private static class NodePanel extends JPanel {

        private JCheckBox check;

        public NodePanel(Color color, String text) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                    JBUI.Borders.empty(0, 3)));

            setAlignmentX(Component.LEFT_ALIGNMENT);

            check = new JCheckBox(text);
            add(check);
        }

        public boolean isSelected() {
            return check.isSelected();
        }

        public void setSelected(boolean selected) {
            check.setSelected(selected);
        }

        public String getText() {
            return check.getText();
        }
    }
}
