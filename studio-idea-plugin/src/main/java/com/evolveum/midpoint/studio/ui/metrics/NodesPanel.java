package com.evolveum.midpoint.studio.ui.metrics;

import com.evolveum.midpoint.studio.impl.metrics.Node;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class NodesPanel extends JPanel {

    public NodesPanel(java.util.List<Node> nodes) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initLayout(nodes);
    }

    private void initLayout(List<Node> nodes) {
        if (nodes == null) {
            return;
        }

        for (Node node : nodes) {
            // todo up status
            
            add(new NodePanel(node.getColor(), true, node.getName()));
        }
    }

    public void refreshNodes(List<Node> nodes) {
        removeAll();

        initLayout(nodes);
    }

    private static class NodePanel extends JPanel {

        private static final Color UP = Color.BLACK;

        private static final Color DOWN = Color.RED;

        private JCheckBox check;

        public NodePanel(Color color, boolean up, String text) {
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

        public void setStatus(boolean up) {
            check.setForeground(up ? UP : DOWN);
        }

        public boolean getStatus() {
            return check.getForeground() == UP;
        }
    }
}
