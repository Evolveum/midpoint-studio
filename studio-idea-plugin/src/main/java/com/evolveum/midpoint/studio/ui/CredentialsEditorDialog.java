package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.Credentials;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CredentialsEditorDialog extends DialogWrapper {

    private Credentials credentials;
    private List<Environment> environments = new ArrayList<>();

    private JTextField key;
    private JTextField username;
    private JPasswordField password;
    private JTextArea description;
    private JPanel root;
    private JComboBox environment;

    public CredentialsEditorDialog(@Nullable Credentials credentials, List<Environment> environments) {
        super(false);
        setTitle(credentials == null ? "Add credentials" : "Edit credentials");

        this.credentials = credentials != null ? credentials : new Credentials();

        if (environments != null) {
            this.environments.addAll(environments);
        }

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
        ComboBoxModel model = new ListComboBoxModel(environments);
        environment.setModel(model);

        key.setText(credentials.getKey());

        if (credentials.getEnvironment() != null) {
            Environment env = null;
            for (Environment e : environments) {
                if (Objects.equals(e.getId(), credentials.getEnvironment())) {
                    env = e;
                }
            }
            environment.setSelectedItem(env);
        }

        username.setText(credentials.getUsername());
        password.setText(credentials.getPassword());
        description.setText(credentials.getDescription());
    }

    private void fillCredentials() {
        credentials.setKey(key.getText());

        Environment env = (Environment) environment.getSelectedItem();
        credentials.setEnvironment(env != null ? env.getId() : null);

        credentials.setUsername(username.getText());
        credentials.setPassword(new String(password.getPassword()));
        credentials.setDescription(description.getText());
    }

    private void createUIComponents() {
        environment = new ComboBox();
        environment.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Environment) {
                    value = ((Environment) value).getName();
                } else if (value == null) {
                    value = "All Environments";
                }

                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }
}
