package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.evolveum.midpoint.studio.util.ObjectTypesConverter;
import com.intellij.ui.TitledSeparator;

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
    private JTextField typesDownloadLimit;
    private JTextField typesIncluded;
    private JTextField typesExcluded;
    private TitledSeparator downloadByTypeSeparator;

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

        ObjectTypesConverter converter = new ObjectTypesConverter();
        typesIncluded.setText(converter.toString(settings.getDownloadTypesInclude()));
        typesExcluded.setText(converter.toString(settings.getDownloadTypesExclude()));

        typesDownloadLimit.setText(Integer.toString(settings.getTypesToDownloadLimit()));
    }

    public MidPointSettings getSettings() {
        MidPointSettings settings = MidPointSettings.createDefaultSettings();
        settings.setProjectId(this.settings.getProjectId());    // we don't want to replace projectId with random id
        settings.setDowloadFilePattern(downloadPattern.getText());
        settings.setGeneratedFilePattern(generatedPattern.getText());
        settings.setPrintRestCommunicationToConsole(logRestCommunication.isSelected());

        ObjectTypesConverter converter = new ObjectTypesConverter();
        settings.setDownloadTypesInclude(converter.fromString(typesIncluded.getText()));
        settings.setDownloadTypesExclude(converter.fromString(typesExcluded.getText()));

        settings.setTypesToDownloadLimit(Integer.parseInt(typesDownloadLimit.getText()));

        return settings;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
