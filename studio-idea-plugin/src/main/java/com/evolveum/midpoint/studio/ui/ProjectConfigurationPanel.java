package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.ProjectSettings;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private JLabel oldPasswordLabel;
    private JPasswordField oldPassword;
    private JButton importFromEclipse;

    private ProjectSettings settings;

    private boolean allowMasterPasswordReset;

    public ProjectConfigurationPanel(ProjectSettings settings, boolean allowMasterPasswordReset) {
        super(new BorderLayout());

        this.settings = settings;
        this.allowMasterPasswordReset = allowMasterPasswordReset;

        add(root, BorderLayout.CENTER);

        oldPasswordLabel.setVisible(allowMasterPasswordReset);
        oldPassword.setVisible(allowMasterPasswordReset);

        importFromEclipse.addActionListener(e -> importFromEclipsePerformed(e));
        importFromEclipse.setVisible(isImportFromEclipseVisible());
    }

    protected boolean isImportFromEclipseVisible() {
        return false;
    }

    protected void importFromEclipsePerformed(ActionEvent evt) {

    }

    public boolean isModified() {
        return midpointSettingsPanel.isModified() || environmentsPanel.isModified();
    }

    public ProjectSettings getSettings() {
        ProjectSettings settings = new ProjectSettings();

        settings.setMidPointSettings(midpointSettingsPanel.getSettings());
        settings.setEnvironmentSettings(environmentsPanel.getFullSettings());

        if (password1.getPassword().length > 0) {
            settings.setMasterPassword(new String(password1.getPassword()));
        }
        if (oldPassword.getPassword().length > 0) {
            settings.setOldMasterPassword(new String(oldPassword.getPassword()));
        }

        return settings;
    }

    private void createUIComponents() {
        midpointSettingsPanel = new MidPointSettingsPanel(settings.getMidPointSettings());
        environmentsPanel = new EnvironmentsPanel(null, settings.getMidPointSettings(), settings.getEnvironmentSettings());
    }

    /**
     * @return {@code true} if input is valid, {@code false} otherwise
     * @throws ConfigurationException if input is not valid and needs user attention. Exception message will be displayed to user
     */
    public boolean validateData() throws ConfigurationException {
        String oldPwd = oldPassword.getPassword() != null ? new String(oldPassword.getPassword()) : null;
        if (StringUtils.isNotEmpty(oldPwd)) {
            String projectId = settings.getMidPointSettings().getProjectId();
            String currentPwd = MidPointUtils.getPassword(projectId);
            if (!Objects.equals(oldPwd, currentPwd)) {
                throw new ConfigurationException("Old password doesn't match one that is stored in keychain with id " + projectId);
            }
        }

        String pwd1 = password1.getPassword() != null ? new String(password1.getPassword()) : null;
        String pwd2 = password2.getPassword() != null ? new String(password2.getPassword()) : null;

        if ((allowMasterPasswordReset && StringUtils.isNotEmpty(oldPwd) && StringUtils.isAnyEmpty(pwd1, pwd2))
                || (!allowMasterPasswordReset && StringUtils.isAnyEmpty(pwd1, pwd2))) {
            throw new ConfigurationException("Master password not filled in");
        }

        if (!Objects.equals(pwd1, pwd2)) {
            throw new ConfigurationException("Master passwords doesn't match");
        }

        midpointSettingsPanel.validateData();

        return true;
    }

    public void updateSettings() {
        settings.setEnvironmentSettings(environmentsPanel.getFullSettings());
        settings.setMidPointSettings(midpointSettingsPanel.getSettings());
        settings.setMasterPassword(new String(password1.getPassword()));
        settings.setOldMasterPassword(new String(oldPassword.getPassword()));
    }

    public void clearPasswords() {
        oldPassword.setText("");
        password1.setText("");
        password2.setText("");
    }
}
