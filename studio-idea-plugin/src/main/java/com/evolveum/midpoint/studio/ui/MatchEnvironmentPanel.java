package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MatchEnvironmentPanel extends BorderLayoutPanel {

    private JPanel root;
    private JLabel environment;
    private JComboBox environments;

    public MatchEnvironmentPanel(String environment, List<Environment> environments) {
        add(root, BorderLayout.CENTER);

        this.environment.setText(environment);

        List<Environment> environmentList = new ArrayList<>();
        environmentList.add(null);

        if (environments != null) {
            environmentList.addAll(environments);
        }
        this.environments.setModel(new ListComboBoxModel(environmentList));
    }

    public Environment getEnvironment() {
        return (Environment) environments.getSelectedItem();
    }

    private void createUIComponents() {
        environments = new ComboBox();
        environments.setRenderer(SimpleListCellRenderer.<Environment>create("All Environments", e -> e != null ? e.getName() : null));
    }
}
