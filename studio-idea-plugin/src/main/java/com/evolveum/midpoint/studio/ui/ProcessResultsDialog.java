package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.browse.ComboQueryType;
import com.evolveum.midpoint.studio.impl.browse.*;
import com.evolveum.midpoint.studio.util.EnumComboBoxModel;
import com.evolveum.midpoint.studio.util.LocalizedRenderer;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ide.actions.ActionsCollector;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogEarthquakeShaker;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessResultsDialog extends DialogWrapper {

    public static final List<Generator> GENERATORS = Arrays.asList(
            new BulkActionGenerator(BulkActionGenerator.Action.RECOMPUTE),
            new BulkActionGenerator(BulkActionGenerator.Action.ENABLE),
            new BulkActionGenerator(BulkActionGenerator.Action.DISABLE),
            new BulkActionGenerator(BulkActionGenerator.Action.DELETE),
            new BulkActionGenerator(BulkActionGenerator.Action.MODIFY),
            new BulkActionGenerator(BulkActionGenerator.Action.ASSIGN_TO_THIS),
            new BulkActionGenerator(BulkActionGenerator.Action.ASSIGN_THIS),
            new BulkActionGenerator(BulkActionGenerator.Action.EXECUTE_SCRIPT),
            new BulkActionGenerator(BulkActionGenerator.Action.NOTIFY),
            new BulkActionGenerator(BulkActionGenerator.Action.LOG),
            new BulkActionGenerator(BulkActionGenerator.Action.TEST_RESOURCE),
            new BulkActionGenerator(BulkActionGenerator.Action.VALIDATE),
            new TaskGenerator(TaskGenerator.Action.RECOMPUTE),
            new TaskGenerator(TaskGenerator.Action.DELETE),
            new TaskGenerator(TaskGenerator.Action.MODIFY),
            new TaskGenerator(TaskGenerator.Action.SHADOW_CHECK),
            new QueryGenerator(),
            new AssignmentGenerator(),
            new RefGenerator("targetRef", ObjectTypes.OBJECT),
            new RefGenerator("resourceRef", ObjectTypes.RESOURCE),
            new RefGenerator("linkRef", ObjectTypes.SHADOW),
            new ConnectorRefGenerator(),
            new RefGenerator("parentOrgRef", ObjectTypes.ORG),
            new RefGenerator("ownerRef", ObjectTypes.ORG)
    );

    public static final int GENERATE_EXIT_CODE = 1000;

    private JComboBox<Generator> generate;
    private JComboBox<Execution> execution;
    private JTextField n;
    private JCheckBox wrapCreatedBulkActionCheckBox;
    private JCheckBox createTasksInSuspendedCheckBox;
    private JCheckBox executeInRawModeCheckBox;
    private JCheckBox executeInDryRunCheckBox;
    private JCheckBox runtimeResolutionCheckBox;
    private JCheckBox useSymbolicReferencesCheckBox;
    private JPanel root;

    private String query;
    private ComboQueryType.Type queryType;
    private ObjectTypes type;
    private List<ObjectType> selected;

    private ProcessResultsOptions options;

    public ProcessResultsDialog(@NotNull ProcessResultsOptions options, String query,
                                ComboQueryType.Type queryType, ObjectTypes type, List<ObjectType> selected) {
        super(false);
        setTitle("Process results");

        setOKButtonText("Execute");
        setOKButtonTooltip("Process results");

        this.options = options;
        this.query = query;
        this.queryType = queryType;
        this.type = type;
        this.selected = selected;

        init();

        initInputFields();
    }

    private void initInputFields() {
        EnumComboBoxModel em = new EnumComboBoxModel(Execution.class, true);
        execution.setModel(em);
        execution.setRenderer(new LocalizedRenderer());

        execution.addItemListener(e -> n.setEnabled(Execution.OID_BATCHES_BY_N.equals(execution.getSelectedItem())));

        execution.setSelectedItem(options.getExecution());

        generate.setModel(new ListComboBoxModel<Generator>(GENERATORS));
        generate.getModel().setSelectedItem(options.getGenerator());
        generate.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Generator g = (Generator) value;
                value = g != null ? g.getLabel() : value;

                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        GeneratorOptions opts = options.getOptions();
        n.setText(Integer.toString(opts.getBatchSize()));
        createTasksInSuspendedCheckBox.setSelected(opts.isCreateSuspended());
        executeInDryRunCheckBox.setSelected(opts.isDryRun());
        useSymbolicReferencesCheckBox.setSelected(opts.isSymbolicReferences());
        runtimeResolutionCheckBox.setSelected(opts.isSymbolicReferencesRuntime());
        wrapCreatedBulkActionCheckBox.setSelected(opts.isWrapActions());
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

    private void doGenerateAction(ActionEvent evt) {
        close(GENERATE_EXIT_CODE);
    }

    public ProcessResultsOptions buildOptions() {
        ProcessResultsOptions options = new ProcessResultsOptions();
        options.setOptions(buildGeneratorOptions());
        options.setExecution((Execution) execution.getSelectedItem());
        options.setGenerator((Generator) generate.getSelectedItem());

        return options;
    }

    private GeneratorOptions buildGeneratorOptions() {
        GeneratorOptions opts = new GeneratorOptions();
        opts.setBatchSize(Integer.parseInt(n.getText()));
        opts.setCreateSuspended(createTasksInSuspendedCheckBox.isSelected());
        opts.setDryRun(executeInDryRunCheckBox.isSelected());
        opts.setRaw(executeInRawModeCheckBox.isSelected());
        opts.setSymbolicReferences(useSymbolicReferencesCheckBox.isSelected());
        opts.setSymbolicReferencesRuntime(runtimeResolutionCheckBox.isSelected());
        opts.setWrapActions(wrapCreatedBulkActionCheckBox.isSelected());

        Execution exec = (Execution) execution.getSelectedItem();
        if (exec == null) {
            exec = Execution.OID_ONE_BATCH;
        }
        switch (exec) {
            case OID_ONE_BATCH:
                opts.setBatchByOids(true);
                opts.setBatchSize(selected.size());
                break;
            case OID_ONE_BY_ONE:
                opts.setBatchByOids(true);
                opts.setBatchSize(1);
                break;
            case OID_BATCHES_BY_N:
                opts.setBatchByOids(true);
                opts.setBatchSize(Integer.parseInt(n.getText()));
                break;
            case ORIGINAL_QUERY:
                opts.setBatchUsingOriginalQuery(true);
                opts.setOriginalQuery(query);
                opts.setOriginalQueryTypes(Arrays.asList(type));
                break;
        }

        return opts;
    }

    public boolean isGenerate() {
        return getExitCode() == GENERATE_EXIT_CODE;
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
            doGenerateAction(e);
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
