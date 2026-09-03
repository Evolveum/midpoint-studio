/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.action;

import com.intellij.ui.components.ActionLink;

import javax.swing.*;

public class ActionPanel extends JPanel {
    private final JButton apply = new JButton("Apply");
    private final JButton discard = new JButton("Discard");
    private final ActionLink details = new ActionLink("Show XML");

    public ActionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(apply);
        add(Box.createHorizontalStrut(5));
        add(discard);
        add(Box.createHorizontalStrut(5));
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
