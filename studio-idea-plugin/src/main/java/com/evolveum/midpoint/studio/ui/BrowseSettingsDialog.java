package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.browse.Constants;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseSettingsDialog extends DialogWrapper {

    private JPanel root;
    private JTextField from;
    private JTextField pageSize;
    private JComboBox nameFilterCombo;

    private String nameFilterType;
    private Paging paging;

    public BrowseSettingsDialog(QName nameFilterType, Paging paging) {
        super(false);
        setTitle("Paging");

        this.nameFilterType = Objects.equals(Constants.Q_SUBSTRING, nameFilterType) ? "Substring" : "Equal";
        this.paging = paging != null ? paging.copy() : new Paging();

        populateFields();
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        updateData();

        super.doOKAction();
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> infos = new ArrayList<>();

        ValidationInfo vi = validateInput(from, "From");
        if (vi != null) {
            infos.add(vi);
        }
        vi = validateInput(pageSize, "Page size");
        if (vi != null) {
            infos.add(vi);
        }

        if (!infos.isEmpty()) {
            return infos;
        }

        // todo validate real integers now

        return infos;
    }

    private int parseInt(JTextField component) {
        String value = component.getText();
        return Integer.parseInt(value);
    }

    private ValidationInfo validateInput(JTextField component, String name) {
        String value = component.getText();
        if (value == null) {
            return new ValidationInfo(name + " can't be empty");
        }

        if (!value.matches("[0-9]+")) {
            return new ValidationInfo(name + " is a number");
        }

        return null;
    }

    public Paging getPaging() {
        return paging;
    }

    public QName getNameFilterType() {
        return Objects.equals("Substring", nameFilterType) ? Constants.Q_SUBSTRING : Constants.Q_EQUAL_Q;
    }

    private void updateData() {
        paging.setFrom(Integer.parseInt(from.getText()));
        paging.setPageSize(Integer.parseInt(pageSize.getText()));

        nameFilterType = (String) nameFilterCombo.getSelectedItem();
    }

    private void populateFields() {
        from.setText(Integer.toString(paging.getFrom()));
        pageSize.setText(Integer.toString(paging.getPageSize()));

        nameFilterCombo.setSelectedItem(nameFilterType);
    }
}
