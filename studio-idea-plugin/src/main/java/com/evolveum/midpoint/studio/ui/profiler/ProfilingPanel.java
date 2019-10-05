package com.evolveum.midpoint.studio.ui.profiler;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProfilingPanel extends JPanel {

    private JPanel root;

    public ProfilingPanel() {
        super(new BorderLayout());
        add(root, BorderLayout.CENTER);
    }
}
