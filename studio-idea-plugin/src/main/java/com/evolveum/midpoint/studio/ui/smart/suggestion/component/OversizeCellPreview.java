/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.table.JBTable;
import jdk.jfr.Experimental;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

@Experimental
public class OversizeCellPreview {

    private static final int PREVIEW_THRESHOLD = 50;

    public static void install(JBTable table) {

        table.addMouseMotionListener(new MouseMotionAdapter() {

            JBPopup activePopup = null;

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row < 0 || col < 0) {
                    closePopup();
                    return;
                }

                Object value = table.getValueAt(row, col);
                if (value == null) {
                    closePopup();
                    return;
                }

                String text = value.toString();
                if (text.length() <= PREVIEW_THRESHOLD) {
                    closePopup();
                    return;
                }

                if (activePopup != null && activePopup.isVisible()) {
                    return;
                }

                showPopup(table, e.getLocationOnScreen(), text);
            }

            private void showPopup(JComponent parent, Point screenPoint, String text) {
                closePopup();

                JTextArea area = new JTextArea(text);
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(new JScrollPane(area), BorderLayout.CENTER);
                panel.setPreferredSize(new Dimension(400, 200));

                activePopup = JBPopupFactory.getInstance()
                        .createComponentPopupBuilder(panel, area)
                        .setResizable(true)
                        .setMovable(true)
                        .setRequestFocus(false)
                        .setCancelOnClickOutside(true)
                        .setCancelKeyEnabled(true)
                        .createPopup();

                activePopup.showInScreenCoordinates(parent, screenPoint);
            }

            private void closePopup() {
                if (activePopup != null && activePopup.isVisible()) {
                    activePopup.cancel();
                }
                activePopup = null;
            }
        });
    }
}
