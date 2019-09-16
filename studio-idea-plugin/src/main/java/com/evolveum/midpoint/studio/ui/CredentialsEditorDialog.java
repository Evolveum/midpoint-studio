package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.evolveum.midpoint.studio.impl.Credentials;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CredentialsEditorDialog extends DialogWrapper {

    private Credentials credentials;

    private JTextField key;
    private JTextField username;
    private JPasswordField password;
    private JTextArea description;
    private JPanel root;

    public CredentialsEditorDialog(@Nullable Credentials credentials) {
        super(false);
        setTitle(credentials == null ? "Add credentials" : "Edit credentials");

        if (credentials == null) {
            credentials = new Credentials();
        }
        this.credentials = credentials;

        fillInFields();

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return key;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> infos = new ArrayList<>();

        if (StringUtils.isEmpty(key.getText())) {
            infos.add(new ValidationInfo("Key must not be empty"));
        }

        return infos;
    }

    @Override
    protected void doOKAction() {
        fillCredentials();

        super.doOKAction();
    }

    private void fillInFields() {
        key.setText(credentials.getKey());
        username.setText(credentials.getUsername());
        password.setText(credentials.getPassword());
        description.setText(credentials.getDescription());
    }

    private void fillCredentials() {
        credentials.setKey(key.getText());
        credentials.setUsername(username.getText());
        credentials.setPassword(new String(password.getPassword()));
        credentials.setDescription(description.getText());
    }
}
