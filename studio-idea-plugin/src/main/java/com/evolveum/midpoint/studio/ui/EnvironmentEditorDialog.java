package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.client.ProxyType;
import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.studio.util.Selectable;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentEditorDialog extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(EnvironmentEditorDialog.class);

    private JPanel root;
    private JTextField name;
    private JTextField url;
    private JTextField username;
    private JPasswordField password;
    private JCheckBox ignoreSslErrors;
    private JCheckBox selected;
    private JButton chooseButton;
    private JLabel colorLabel;
    private TextFieldWithBrowseButton properties;
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JComboBox proxyType;
    private JPasswordField proxyPassword;
    private JTextField proxyUsername;
    private JLabel testConnection;
    private JPanel colorPanel;
    private JCheckBox useHttp2;

    private Project project;

    private MidPointConfiguration settings;

    private Selectable<Environment> selectable;

    public EnvironmentEditorDialog(Project project, MidPointConfiguration settings, @Nullable Selectable<Environment> environment) {
        super(false);

        this.project = project;
        this.settings = settings;

        setTitle(environment == null ? "Add environment" : "Edit environment");

        root.setMinimumSize(new Dimension(500, 600));

        colorPanel.setBorder(JBUI.Borders.empty(3, 3));
        testConnection.setBorder(JBUI.Borders.empty(3, 3));

        Color background = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        if (background != null) {
            testConnection.setOpaque(true);
            testConnection.setBackground(background);
        }

        properties.addBrowseFolderListener("Select source folder", "Properties file where MidPoint object xml file parameters are stored", project,
                FileChooserDescriptorFactory.createSingleFileDescriptor("properties"));

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
        populateBean();

        super.doOKAction();
    }

    private void populateBean() {
        selectable.setSelected(selected.isSelected());

        Environment environment = selectable.getObject();
        populateEnvironment(environment);
    }

    private void populateEnvironment(Environment environment) {
        environment.setName(name.getText());
        environment.setUrl(url.getText());
        environment.setUsername(username.getText());
        environment.setPassword(String.copyValueOf(password.getPassword()));
        environment.setIgnoreSslErrors(ignoreSslErrors.isSelected());
        environment.setUseHttp2(useHttp2.isSelected());

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
        useHttp2.setSelected(environment.isUseHttp2());

        proxyHost.setText(environment.getProxyServerHost());
        proxyPort.setText(environment.getProxyServerPort() != null ? environment.getProxyServerPort().toString() : null);
        proxyType.setSelectedItem(environment.getProxyServerType());
        proxyUsername.setText(environment.getProxyUsername());
        proxyPassword.setText(environment.getProxyPassword());

        properties.setText(environment.getPropertiesFilePath());
        colorLabel.setBackground(environment.getAwtColor());
    }

    @NotNull
    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                getCancelAction(),
                getOKAction(),
                getTestConnectionAction(),
        };
    }

    private Action getTestConnectionAction() {
        return new TestConnectionAction() {

//            @Override
//            public boolean isEnabled() {
//                return super.isEnabled() && project != null;
//            }
        };
    }

    private void doTestConnectionAction(ActionEvent evt) {
        Task.Backgroundable task = new Task.Backgroundable(project, "Saving Operation Result Xml") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new RunnableUtils.PluginClasspathRunnable() {

                    @Override
                    public void runWithPluginClassLoader() {
                        executeTestConnection(project, testConnection);
                    }
                }.run();
            }
        };
        // todo run synchronously
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void executeTestConnection(Project project, JLabel testConnection) {
        Environment env = new Environment();
        populateEnvironment(env);

        try {
            MidPointClient client = new MidPointClient(project, env, settings);
            TestConnectionResult result = client.testConnection();

            if (result.success()) {
                updateInAwtThread(ConsoleViewContentType.NORMAL_OUTPUT_KEY.getDefaultAttributes().getForegroundColor(), "Version: " + result.version() + ", revision: " + result.revision());
            } else {
                String msg = result.exception() != null ? result.exception().getMessage() : null;
                updateInAwtThread(ConsoleViewContentType.LOG_ERROR_OUTPUT_KEY.getDefaultAttributes().getForegroundColor(), msg);
            }
        } catch (Exception ex) {
            LOG.error("Couldn't test connection", ex);

            updateInAwtThread(ConsoleViewContentType.LOG_ERROR_OUTPUT_KEY.getDefaultAttributes().getForegroundColor(), ex.getMessage());
        }
    }

    private void updateInAwtThread(Color color, String text) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            testConnection.setForeground(color);
            testConnection.setText(text);
        });
    }

    private class TestConnectionAction extends DialogWrapperAction {

        protected TestConnectionAction() {
            super("Test Connection");
        }

        @Override
        protected void doAction(ActionEvent e) {
            List<ValidationInfo> infoList = doValidateAll();
            if (!infoList.isEmpty()) {
                ValidationInfo info = infoList.get(0);
                if (info.component != null && info.component.isVisible()) {
                    IdeFocusManager.getInstance(null).requestFocus(info.component, true);
                }

                startTrackingValidation();
                if (infoList.stream().anyMatch(info1 -> !info1.okEnabled)) return;
            }
            doTestConnectionAction(e);
        }
    }
}
