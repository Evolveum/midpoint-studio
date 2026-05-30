package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultDialog extends DialogWrapper {

    private final BorderLayoutPanel panel;

    public OperationResultDialog(@NotNull OperationResult result) {
        super(false);

        setTitle("Result: " + result.getOperation());
        setSize(1000, 500);

        this.panel = new BorderLayoutPanel();

        OperationResultTableModel model = new OperationResultTableModel(result);
        DefaultTreeTable<OperationResultTableModel> table = new DefaultTreeTable<>(model);
        table.getTree().setRootVisible(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status column: colour-coded by status value
        table.getColumnModel().getColumn(1).setCellRenderer(createStatusRenderer());

        // Expand all rows initially
        for (int i = 0; i < table.getRowCount(); i++) {
            table.getTree().expandRow(i);
        }

        JComponent toolbar = initToolbar(table, result);
        this.panel.addToTop(toolbar);
        this.panel.addToCenter(new JBScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        init();
    }

    private TableCellRenderer createStatusRenderer() {
        return new ColoredTableCellRenderer() {

            @Override
            protected void customizeCellRenderer(
                    @NotNull JTable table, @Nullable Object value,
                    boolean selected, boolean hasFocus, int row, int column) {

                append(value != null ? value.toString() : "");

                if (!(value instanceof OperationResultStatus status)) {
                    return;
                }

                Color fg = switch (status) {
                    case SUCCESS, HANDLED_ERROR -> JBColor.GREEN.darker();
                    case PARTIAL_ERROR, FATAL_ERROR -> JBColor.RED.darker();
                    case IN_PROGRESS -> JBColor.BLUE.darker();
                    case NOT_APPLICABLE, UNKNOWN, WARNING -> JBColor.ORANGE.darker();
                };
                setForeground(fg);
            }
        };
    }

    private JComponent initToolbar(DefaultTreeTable<OperationResultTableModel> table, OperationResult result) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> {
            for (int i = 0; i < table.getRowCount(); i++) {
                table.getTree().expandRow(i);
            }
        }));
        group.add(MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> {
            for (int i = table.getRowCount() - 1; i >= 0; i--) {
                table.getTree().collapseRow(i);
            }
        }));
        group.add(new Separator());
        group.add(MidPointUtils.createAnAction("Export Result", AllIcons.Actions.Download, e -> openSaveResultDialog(e, result)));

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("OperationResultDialogToolbar", group, true);
        toolbar.setTargetComponent(panel);
        return toolbar.getComponent();
    }

    private void openSaveResultDialog(AnActionEvent e, OperationResult result) {
        Project project = e.getProject();

        FileSaverDialog saver = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save Operation Result As Xml", "Save to", "xml"), project);

        VirtualFileWrapper target = saver.save((com.intellij.openapi.vfs.VirtualFile) null, project.getName() + ".xml");
        if (target != null) {
            Task.Backgroundable task = new Task.Backgroundable(project, "Saving Operation Result Xml") {

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    new RunnableUtils.PluginClasspathRunnable() {

                        @Override
                        public void runWithPluginClassLoader() {
                            saveResult(project, indicator, target, result);
                        }
                    }.run();
                }
            };
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        }
    }

    private void saveResult(Project project, ProgressIndicator indicator, VirtualFileWrapper fileWrapper, OperationResult result) {
        MidPointService mm = MidPointService.get(project);

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment environment = em.getSelected();

        RunnableUtils.runWriteActionAndWait(() -> {
            File file = fileWrapper.getFile();

            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } catch (IOException ex) {
                mm.printToConsole(environment, OperationResultDialog.class, "Couldn't create file " + file.getPath(), ex);
            }

            try (BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(fileWrapper.getVirtualFile().getOutputStream(this), fileWrapper.getVirtualFile().getCharset()))) {

                MidPointClient client = new MidPointClient(project, environment);
                PrismContext ctx = client.getPrismContext();
                PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);
                String xml = serializer.serializeAnyData(result.createOperationResultType(), SchemaConstantsGenerated.C_OPERATION_RESULT);
                IOUtils.write(xml, out);
            } catch (IOException | SchemaException ex) {
                mm.printToConsole(environment, OperationResultDialog.class, "Couldn't save operation result", ex);
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
