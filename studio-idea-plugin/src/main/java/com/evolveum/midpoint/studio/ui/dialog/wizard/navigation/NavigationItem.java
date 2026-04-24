package com.evolveum.midpoint.studio.ui.dialog.wizard.navigation;

import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardStep;
import com.intellij.icons.AllIcons;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import com.intellij.util.ui.Animator;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavigationItem<CT> extends JPanel {

    private final JPanel childrenPanel = new JPanel();
    private final JLabel arrowLabel = new JLabel();
    private final WizardStep<CT> step;
    private boolean expanded = false;

    private final int ITEM_HEIGHT = 60;

    public NavigationItem(WizardStep<CT> step, int level, Consumer<WizardStep<CT>> onSelect) {
        this.step = step;

        setLayout(new BorderLayout());
        setSizeItem(this, Integer.MAX_VALUE, ITEM_HEIGHT);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        add(createHeader(level), BorderLayout.NORTH);
        installListeners(onSelect, level);
    }

    private void initStyleTile(JPanel panel) {
        panel.setBackground(JBColor.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(Gray._200, 2, true),
                JBUI.Borders.empty(15)
        ));
    }

    private JPanel createHeader(int level) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        configureArrow(level);

        JLabel title = new JLabel(step.getTitle());
        title.setForeground(JBColor.foreground());

        header.add(arrowLabel);
        header.add(title);

        initStyleTile(header);

        return header;
    }

    private void configureArrow(int level) {
        arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, level * 14, 0, 0));

        if (!step.getChildren().isEmpty()) {
            arrowLabel.setIcon(AllIcons.General.ArrowRight);
        } else {
            arrowLabel.setIcon(null);
        }
    }

    private JPanel addChildren(int level, Consumer<WizardStep<CT>> onSelect) {
        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        childrenPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        childrenPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        int verticalMargin = 10;
        int count = 0;

        for (WizardStep<CT> child : step.getChildren()) {
            childrenPanel.add(new NavigationItem<>(child, level + 1, onSelect));
            childrenPanel.add(Box.createVerticalStrut(verticalMargin));

            count++;
        }

        animateSlide("SlideAnimationDown",
                this,
                ITEM_HEIGHT,
                ITEM_HEIGHT + (count * (ITEM_HEIGHT + verticalMargin)),
                300);

        return childrenPanel;
    }

    private void installListeners(Consumer<WizardStep<CT>> onSelect, int level) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.consume(step);

                if(!step.getChildren().isEmpty()) {
                    expanded = !expanded;

                    arrowLabel.setIcon(
                        expanded ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight
                    );

                    if (expanded) {
                        add(addChildren(level, onSelect));
                    } else {
                        if (e.getSource() instanceof NavigationItem<?> clickedNavigationItem) {
                            if (clickedNavigationItem.getComponentCount() >= 2 &&
                                    clickedNavigationItem.getComponent(1) instanceof JPanel currentChildrenPanel
                            ) {
                                animateSlide("SlideAnimationUp",
                                        clickedNavigationItem,
                                        getHeight(),
                                        ITEM_HEIGHT,
                                        300);
                                currentChildrenPanel.removeAll();
                            }
                        }
                    }
                }
            }
        });
    }

    private void animateSlide(String name, JComponent component, int start, int end, int duration) {
        Animator animator = new Animator(name, duration, duration, false) {
            @Override
            public void paintNow(int frame, int totalFrames, int cycle) {
                float progress = (float) frame / totalFrames;
                float eased = (float)(1 - Math.pow(1 - progress, 3));
                int height = (int) (start + (end - start) * eased);

                setSizeItem(component, component.getPreferredSize().width, height);
                component.revalidate();
                component.repaint();
            }

            @Override
            protected void paintCycleEnd() {
                component.setPreferredSize(new Dimension(component.getWidth(), end));
                component.revalidate();
            }
        };

        animator.resume();
    }

    private JLabel createStatusBadge(WizardStep<CT> step) {
        JLabel badge = new JLabel(String.valueOf(step.getStatus()));

        badge.setFont(badge.getFont().deriveFont(11f));
        badge.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));

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

    void setSizeItem(Component component, int weight, int height) {
        component.setPreferredSize(new Dimension(weight, height));
        component.setMinimumSize(new Dimension(weight, height));
        component.setMaximumSize(new Dimension(weight, height));
    }
}