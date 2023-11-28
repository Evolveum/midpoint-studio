package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang3.StringUtils;
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
public class EncryptedPropertyEditorDialog extends DialogWrapper {

    public static final String ALL_ENVIRONMENTS = "All Environments";

    private EncryptedProperty property;

    private List<Environment> environments = new ArrayList<>();

    private JTextField key;
    private JTextField password;
    private JTextArea description;
    private JPanel root;
    private JComboBox environment;

    public EncryptedPropertyEditorDialog(@Nullable EncryptedProperty property, List<Environment> environments) {
        super(false);
        setTitle(property == null ? "Add Encrypted Property" : "Edit Encrypted Property");

        this.property = property != null ? property : new EncryptedProperty();

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

    public EncryptedProperty getEncryptedProperty() {
        return property;
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
        populateProperty();

        super.doOKAction();
    }

    private void fillInFields() {
        List<Environment> list = new ArrayList<>();
        list.add(null);
        list.addAll(environments);

        ComboBoxModel model = new ListComboBoxModel(list);
        environment.setModel(model);

        key.setText(property.getKey());

        if (property.getEnvironment() != null) {
            Environment env = null;
            for (Environment e : environments) {
                if (Objects.equals(e.getId(), property.getEnvironment())) {
                    env = e;
                }
            }
            environment.setSelectedItem(env);
        }

        password.setText(property.getValue());
        description.setText(property.getDescription());
    }

    private void populateProperty() {
        property.setKey(key.getText());

        Environment env = (Environment) environment.getSelectedItem();
        property.setEnvironment(env != null ? env.getId() : null);

        property.setValue(password.getText());
        property.setDescription(description.getText());
    }

    private void createUIComponents() {
        environment = new ComboBox();
        environment.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Environment) {
                    value = ((Environment) value).getName();
                } else if (value == null) {
                    value = ALL_ENVIRONMENTS;
                }

                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }
}
