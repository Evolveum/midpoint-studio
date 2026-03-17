package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.intellij.icons.AllIcons;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavigationItem<CT> extends JBPanel<NavigationItem<CT>> {

    private static final int ITEM_HEIGHT = 40;

    private boolean expanded = false;
    private final JBPanel<?> childrenPanel = new JBPanel<>();
    private final JLabel arrowLabel = new JLabel();
    private final JPanel tile = new JPanel(new BorderLayout());
    private final WizardStep<CT> step;

    public NavigationItem(WizardStep<CT> step, int level, Consumer<WizardStep<CT>> onSelect) {
        this.step = step;

        setLayout(new BorderLayout());
        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // FIXME distance between navigation items & dropdown menu
        setPreferredSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));
        tile.setMinimumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));
        tile.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        tile.setOpaque(true);

        tile.setBackground(new JBColor(Gray._235, new Color(60,63,65)));
        tile.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        left.setOpaque(false);

        arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, level * 14, 0, 0));

        if(step.getChildren().isEmpty()) {
            arrowLabel.setIcon(null);
        } else {
            arrowLabel.setIcon(AllIcons.General.ArrowRight);
        }

        JLabel title = new JLabel(step.getTitle());
        title.setForeground(JBColor.foreground());

        left.add(arrowLabel);
        left.add(title);

        tile.add(left, BorderLayout.WEST);
        tile.add(createStatusBadge(step), BorderLayout.EAST);

        add(tile, BorderLayout.NORTH);

        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        childrenPanel.setOpaque(false);
        childrenPanel.setVisible(false);

        add(childrenPanel, BorderLayout.CENTER);

        for (WizardStep<CT> child : step.getChildren()) {
            childrenPanel.add(new NavigationItem<>(child, level + 1, onSelect));
            childrenPanel.add(Box.createVerticalStrut(6));
        }

        installListeners(onSelect);
    }

    private void installListeners(Consumer<WizardStep<CT>> onSelect) {
        tile.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackground(new JBColor(Gray._220, new Color(69,73,74)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBackground(new JBColor(Gray._235, new Color(60,63,65)));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.consume(step);

                if(!step.getChildren().isEmpty()) {
                    toggle();
                }
            }
        });
    }

    private void toggle() {
        expanded = !expanded;

        arrowLabel.setIcon(
                expanded ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight
        );

        childrenPanel.setVisible(expanded);

        revalidate();
        repaint();
    }

    private JLabel createStatusBadge(WizardStep<CT> step) {
        JLabel badge = new JLabel(String.valueOf(step.getStatus()));

        badge.setFont(badge.getFont().deriveFont(11f));
        badge.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));
        badge.setOpaque(true);

        switch (step.getStatus()) {

            case COMPLETE:
                badge.setBackground(new JBColor(new Color(120,200,120), new Color(76,175,80)));
                badge.setForeground(JBColor.WHITE);
                break;

            case IN_PROGRESS:
                badge.setBackground(new JBColor(new Color(120,200,190), new Color(0,150,136)));
                badge.setForeground(JBColor.WHITE);
                break;

            default:
                badge.setBackground(new JBColor(Gray._200, Gray._120));
                badge.setForeground(JBColor.WHITE);
        }

        return badge;
    }
}