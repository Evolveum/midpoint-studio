package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PagingDialog extends DialogWrapper {

    private JPanel root;
    private JTextField from;
    private JTextField to;
    private JTextField pageSize;

    private Paging paging;

    public PagingDialog(Paging paging) {
        super(false);
        setTitle("Paging");

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
        vi = validateInput(to, "To");
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

    private void updateData() {
        paging.setFrom(Integer.parseInt(from.getText()));
        paging.setTo(Integer.parseInt(to.getText()));
        paging.setPageSize(Integer.parseInt(pageSize.getText()));
    }

    private void populateFields() {
        from.setText(Integer.toString(paging.getFrom()));
        to.setText(Integer.toString(paging.getTo()));
        pageSize.setText(Integer.toString(paging.getPageSize()));
    }
}
