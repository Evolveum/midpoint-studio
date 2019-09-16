package com.evolveum.midpoint.studio.ui;

import com.intellij.ide.actions.ActionsCollector;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogEarthquakeShaker;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.IdeFocusManager;
import com.evolveum.midpoint.studio.util.EnumComboBoxModel;
import com.evolveum.midpoint.studio.util.LocalizedRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessResultsDialog extends DialogWrapper {

    private JComboBox generate;
    private JComboBox execution;
    private JTextField n;
    private JCheckBox wrapCreatedBulkActionCheckBox;
    private JCheckBox createTasksInSuspendedCheckBox;
    private JCheckBox executeInRawModeCheckBox;
    private JCheckBox executeInDryRunCheckBox;
    private JCheckBox runtimeResolutionCheckBox;
    private JCheckBox useSymbolicReferencesCheckBox;
    private JPanel root;

    public ProcessResultsDialog() {
        super(false);
        setTitle("Process results");

        setOKButtonText("Execute");
        setOKButtonTooltip("Process results");

        init();

        initInputFields();
    }

    private void initInputFields() {
        EnumComboBoxModel em = new EnumComboBoxModel(Execution.class, true);
        execution.setModel(em);
        execution.setSelectedItem(Execution.OID_ONE_BATCH);
        execution.setRenderer(new LocalizedRenderer());

        execution.addItemListener(e -> n.setEnabled(Execution.OID_BATCHES_BY_N.equals(execution.getSelectedItem())));

        EnumComboBoxModel gm = new EnumComboBoxModel(Generate.class, true);
        generate.setModel(gm);
        generate.getModel().setSelectedItem(gm.getElementAt(0));
        generate.setRenderer(new LocalizedRenderer());

        n.setEnabled(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getOKAction(),
                getGenerateAction(),
        };
    }

    private Action getGenerateAction() {
        return new GenerateAction();
    }

    private void doGenerateAction() {
        // todo implement
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        // todo implement
    }

    private class GenerateAction extends DialogWrapperAction {

        protected GenerateAction() {
            super("Generate XML");
        }

        @Override
        protected void doAction(ActionEvent e) {
            recordAction("DialogGenerateAction");
            List<ValidationInfo> infoList = doValidateAll();
            if (!infoList.isEmpty()) {
                ValidationInfo info = infoList.get(0);
                if (info.component != null && info.component.isVisible()) {
                    IdeFocusManager.getInstance(null).requestFocus(info.component, true);
                }

                if (!Registry.is("ide.inplace.validation.tooltip")) {
                    DialogEarthquakeShaker.shake(getPeer().getWindow());
                }

                startTrackingValidation();
                if (infoList.stream().anyMatch(info1 -> !info1.okEnabled)) return;
            }
            doGenerateAction();
        }

        private void recordAction(String name) {
            recordAction(name, EventQueue.getCurrentEvent());
        }

        private void recordAction(String name, AWTEvent event) {
            if (event instanceof KeyEvent && ApplicationManager.getApplication() != null) {
                ActionsCollector.getInstance().record(name, (KeyEvent) event, getClass());
            }
        }
    }
}
