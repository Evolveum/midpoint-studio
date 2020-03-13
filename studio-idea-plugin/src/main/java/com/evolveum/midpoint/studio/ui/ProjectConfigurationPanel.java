package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.ModuleSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProjectConfigurationPanel extends JPanel {

    private JPanel root;
    private EnvironmentsPanel environmentsPanel;
    private JPasswordField password1;
    private JPasswordField password2;
    private MidPointSettingsPanel midpointSettingsPanel;

    private ModuleSettings settings;

    public ProjectConfigurationPanel(ModuleSettings settings) {
        super(new BorderLayout());

        this.settings = settings;

        add(root, BorderLayout.CENTER);
    }

    public ModuleSettings getSettings() {
        return settings;
    }

    private void createUIComponents() {
        midpointSettingsPanel = new MidPointSettingsPanel(settings.getMidPointSettings());
        environmentsPanel = new EnvironmentsPanel(settings.getEnvironmentSettings());
    }

    /**
     * @return {@code true} if input is valid, {@code false} otherwise
     * @throws ConfigurationException if input is not valid and needs user attention. Exception message will be displayed to user
     */
    public boolean validateData() throws ConfigurationException {
        String pwd1 = password1.getPassword() != null ? new String(password1.getPassword()) : null;
        String pwd2 = password2.getPassword() != null ? new String(password2.getPassword()) : null;

        if (!Objects.equals(pwd1, pwd2)) {
            throw new ConfigurationException("Master passwords doesn't match");
        }

        return true;
    }

    public void updateSettings() {
        settings.setEnvironmentSettings(environmentsPanel.getFullSettings());
        settings.setMidPointSettings(midpointSettingsPanel.getSettings());
        settings.setMasterPassword(new String(password1.getPassword()));
    }
}
