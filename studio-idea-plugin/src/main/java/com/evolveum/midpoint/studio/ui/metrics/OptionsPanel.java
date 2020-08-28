package com.evolveum.midpoint.studio.ui.metrics;

import com.evolveum.midpoint.studio.impl.metrics.MetricsKey;
import com.intellij.util.ui.JBUI;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OptionsPanel extends JPanel {

    public OptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(JBUI.Borders.empty(5));

        initLayout();
    }

    private void initLayout() {
        for (MetricsKey key : MetricsKey.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(key.getDisplayName());

            add(check);
        }
    }
}
