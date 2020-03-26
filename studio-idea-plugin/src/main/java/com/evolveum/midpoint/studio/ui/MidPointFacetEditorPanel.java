package com.evolveum.midpoint.studio.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetEditorPanel extends JPanel {

    private JPanel root;

    public MidPointFacetEditorPanel() {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);
    }
}
