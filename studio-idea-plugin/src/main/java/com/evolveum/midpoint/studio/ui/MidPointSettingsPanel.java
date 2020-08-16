package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.MidPointSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettingsPanel extends JPanel {

    private JTextField downloadPattern;

    private JTextField generatedPattern;

    private JPanel root;
    private JCheckBox logRestCommunication;

    private MidPointSettings settings;

    public MidPointSettingsPanel(MidPointSettings settings) {
        super(new BorderLayout());

        this.settings = settings;

        add(root, BorderLayout.CENTER);

        initInputFields();
    }

    private void initInputFields() {
        downloadPattern.setText(settings.getDowloadFilePattern());
        generatedPattern.setText(settings.getGeneratedFilePattern());
        logRestCommunication.setSelected(settings.isPrintRestCommunicationToConsole());
    }

    public MidPointSettings getSettings() {
        MidPointSettings settings = MidPointSettings.createDefaultSettings();
        settings.setProjectId(this.settings.getProjectId());    // we don't want to replace projectId with random id
        settings.setDowloadFilePattern(downloadPattern.getText());
        settings.setGeneratedFilePattern(generatedPattern.getText());
        settings.setPrintRestCommunicationToConsole(logRestCommunication.isSelected());

        return settings;
    }
}
