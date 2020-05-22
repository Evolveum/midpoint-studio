package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.client.api.ProxyType;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Selectable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentEditorDialog extends DialogWrapper {

    private JPanel root;
    private JTextField name;
    private JTextField url;
    private JTextField username;
    private JPasswordField password;
    private JCheckBox ignoreSslErrors;
    private JCheckBox selected;
    private JButton chooseButton;
    private JLabel colorLabel;
    private JTextField properties;
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JComboBox proxyType;
    private JPasswordField proxyPassword;
    private JTextField proxyUsername;

    private Selectable<Environment> selectable;

    public EnvironmentEditorDialog(@Nullable Selectable<Environment> environment) {
        super(false);
        setTitle(environment == null ? "Add environment" : "Edit environment");

        colorLabel.setBorder(JBUI.Borders.emptyLeft(3));

        if (environment == null) {
            environment = new Selectable<>(new Environment());
            environment.getObject().setAwtColor(MidPointUtils.generateAwtColor());
        }
        this.selectable = environment;

        this.proxyType.setModel(new ListComboBoxModel(Arrays.asList(ProxyType.values())));

        fillInFields();

        init();
        chooseButton.addActionListener(e -> {

            Color newColor = JColorChooser.showDialog(null, "Choose a color", colorLabel.getBackground());
            colorLabel.setBackground(newColor);
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return name;
    }

    public Selectable<Environment> getEnvironment() {
        return selectable;
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> infos = new ArrayList<>();

        if (StringUtils.isEmpty(name.getText())) {
            infos.add(new ValidationInfo("Name must not be empty"));
        }

        if (StringUtils.isEmpty(url.getText())) {
            infos.add(new ValidationInfo("Url must not be empty"));
        }

        if (StringUtils.isEmpty(username.getText())) {
            infos.add(new ValidationInfo("Username must not be empty"));
        }

        String port = proxyPort.getText();
        if (StringUtils.isNotEmpty(port)) {
            if (!StringUtils.isNumeric(port)) {
                infos.add(new ValidationInfo("Proxy port is not a number"));
            } else {
                Integer p = Integer.parseInt(port);
                if (p < 0 || p >= Math.pow(2, 16)) {
                    infos.add(new ValidationInfo("Proxy port is out of range 0-65535"));
                }
            }
        }

        return infos;
    }

    @Override
    protected void doOKAction() {
        fillEnvironment();

        super.doOKAction();
    }

    private void fillEnvironment() {
        selectable.setSelected(selected.isSelected());

        Environment environment = selectable.getObject();

        environment.setName(name.getText());
        environment.setUrl(url.getText());
        environment.setUsername(username.getText());
        environment.setPassword(String.copyValueOf(password.getPassword()));
        environment.setIgnoreSslErrors(ignoreSslErrors.isSelected());

        environment.setProxyServerHost(proxyHost.getText());
        String port = proxyPort.getToolTipText();
        if (StringUtils.isNumeric(port)) {
            environment.setProxyServerPort(Integer.parseInt(proxyPort.getText()));
        }
        environment.setProxyServerType((ProxyType) proxyType.getSelectedItem());
        environment.setProxyUsername(proxyUsername.getText());
        environment.setProxyPassword(new String(proxyPassword.getPassword()));

        environment.setPropertiesFilePath(properties.getText());
        environment.setAwtColor(colorLabel.getBackground());
        if (environment.getColor() == null) {
            environment.setAwtColor(MidPointUtils.generateAwtColor());
        }
    }

    private void fillInFields() {
        selected.setSelected(selectable.isSelected());

        Environment environment = selectable.getObject();

        name.setText(environment.getName());
        url.setText(environment.getUrl());
        username.setText(environment.getUsername());
        password.setText(environment.getPassword());
        ignoreSslErrors.setSelected(environment.isIgnoreSslErrors());

        proxyHost.setText(environment.getProxyServerHost());
        proxyPort.setText(environment.getProxyServerPort() != null ? environment.getProxyServerPort().toString() : null);
        proxyType.setSelectedItem(environment.getProxyServerType());
        proxyUsername.setText(environment.getProxyUsername());
        proxyPassword.setText(environment.getProxyPassword());

        properties.setText(environment.getPropertiesFilePath());
        colorLabel.setBackground(environment.getAwtColor());
    }
}
