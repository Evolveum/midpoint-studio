package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.evolveum.midpoint.studio.util.ObjectTypesConverter;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

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
    private JTextField restClientTimeout;
    private JCheckBox ignoreUknownProperties;

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
        restClientTimeout.setText(Integer.toString(settings.getRestResponseTimeout()));

        ObjectTypesConverter converter = new ObjectTypesConverter();
        typesIncluded.setText(converter.toString(settings.getDownloadTypesInclude()));
        typesExcluded.setText(converter.toString(settings.getDownloadTypesExclude()));

        typesDownloadLimit.setText(Integer.toString(settings.getTypesToDownloadLimit()));
        ignoreUknownProperties.setSelected(settings.isIgnoreMissingKeys());
    }

    public boolean isModified() {
        try {
            validateData();
            return !settings.equals(getSettings());
        } catch (Exception ex) {
            return true;
        }
    }

    public MidPointSettings getSettings() {
        MidPointSettings settings = MidPointSettings.createDefaultSettings();
        settings.setProjectId(this.settings.getProjectId());    // we don't want to replace projectId with random id
        settings.setDowloadFilePattern(downloadPattern.getText());
        settings.setGeneratedFilePattern(generatedPattern.getText());
        settings.setPrintRestCommunicationToConsole(logRestCommunication.isSelected());
        if (StringUtils.isNumeric(restClientTimeout.getText())) {
            settings.setRestResponseTimeout(Integer.parseInt(restClientTimeout.getText()));
        }

        ObjectTypesConverter converter = new ObjectTypesConverter();
        settings.setDownloadTypesInclude(converter.fromString(typesIncluded.getText()));
        settings.setDownloadTypesExclude(converter.fromString(typesExcluded.getText()));

        settings.setTypesToDownloadLimit(Integer.parseInt(typesDownloadLimit.getText()));

        settings.setIgnoreMissingKeys(ignoreUknownProperties.isSelected());

        return settings;
    }

    public void validateData() throws ConfigurationException {
        String downloadLimit = typesDownloadLimit.getText();
        validateInteger(downloadLimit, "Download limit");

        validateTypes(typesIncluded.getText(), "Types to download - Include: ");
        validateTypes(typesExcluded.getText(), "Types to download - Exclude: ");

        String restTimeout = restClientTimeout.getText();
        validateInteger(restTimeout, "Rest client timeout");
    }

    private void validateInteger(String number, String message) throws ConfigurationException {
        if (StringUtils.isNotEmpty(number)) {
            try {
                Integer.parseInt(number);
            } catch (Exception ex) {
                throw new ConfigurationException(message + " not in range (0, " + Integer.MAX_VALUE + ")");
            }
        }
    }

    private void validateTypes(String types, String message) throws ConfigurationException {
        try {
            ObjectTypesConverter.fromString(types, false);
        } catch (RuntimeException ex) {
            throw new ConfigurationException(message + ex.getMessage());
        }
    }

    private void createUIComponents() {
    }
}
