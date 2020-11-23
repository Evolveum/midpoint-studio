package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class JiraCredentialsDialog extends DialogWrapper {

    private JPanel root;

    private JTextField username;
    private JPasswordField password;

    public JiraCredentialsDialog() {
        super(false);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return root;
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }
}
