/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.action;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.ActionLink;

import javax.swing.*;
import java.awt.*;

public class ActionPanel extends JPanel {
    private final JButton apply = new JButton("Apply");
    private final JButton discard = new JButton("Discard");
    private final ActionLink details = new ActionLink("Show xml");

    public ActionPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 5, 2));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(apply);
        add(discard);
        add(details);
    }

    public JButton getApply() {
        return apply;
    }

    public JButton getDiscard() {
        return discard;
    }

    public ActionLink getDetails() {
        return details;
    }
}
