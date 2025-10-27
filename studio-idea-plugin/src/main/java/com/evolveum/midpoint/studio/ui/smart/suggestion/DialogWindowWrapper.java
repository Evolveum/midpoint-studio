package com.evolveum.midpoint.studio.ui.smart.suggestion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dominik.
 */
public class DialogWindowWrapper extends DialogWrapper {

    private final JComponent component;
    private final DialogWindowActionHandler actionHandler;

    public DialogWindowWrapper(
            Project project,
            String title,
            JComponent component,
            DialogWindowActionHandler actionHandler
    ) {
        super(project, true);
        this.component = component;
        this.actionHandler = actionHandler;
        setTitle(title);
        init();

        if (actionHandler != null) {
            setOKButtonText(actionHandler.getOkButtonTitle());
            setCancelButtonText(actionHandler.getCancelButtonTitle());
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.component;
    }

    @Override
    protected void doOKAction() {
        actionHandler.onOk();
        super.doOKAction();
    }

    @Override
    protected void applyFields() {
        actionHandler.onApply();
        super.applyFields();
    }

    @Override
    public void doCancelAction() {
        actionHandler.onCancel();
        super.doCancelAction();
    }
}
