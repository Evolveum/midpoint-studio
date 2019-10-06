package com.evolveum.midpoint.studio.ui.result;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class KeyValuePanel extends JPanel {

    private JPanel root;
    private JLabel label;
    private JLabel value;

    public KeyValuePanel(String label, String value) {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);

        this.label.setText(label);
        this.value.setText(value);
    }
}
