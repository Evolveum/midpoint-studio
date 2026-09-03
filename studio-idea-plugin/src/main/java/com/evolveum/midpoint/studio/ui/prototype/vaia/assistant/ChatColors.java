package com.evolveum.midpoint.studio.ui.prototype.vaia.assistant;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import java.awt.*;

/**
 * Created by Dominik.
 */
public class ChatColors {
    public static final JBColor MESSAGE_BACKGROUND_USER = new JBColor(
            new Color(111, 108, 108), // Light mode (light green)
            new Color(111, 108, 108) // Dark mode (dark green)
    );

    public static final JBColor MESSAGE_BACKGROUND_BOT = new JBColor(
            new Color(60, 63, 65), // Light mode (light gray)
            new Color(60, 63, 65) // Dark mode (dark gray)
    );

    public static final JBColor MESSAGE_TEXT_COLOR = new JBColor(
            Color.BLACK,  // Light mode
            Color.WHITE   // Dark mode
    );
}