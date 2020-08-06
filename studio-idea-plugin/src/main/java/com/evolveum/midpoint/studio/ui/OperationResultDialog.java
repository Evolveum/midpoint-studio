package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointManager;
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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultDialog extends DialogWrapper {

    private BorderLayoutPanel panel;

    public OperationResultDialog(@NotNull OperationResult result) {
        super(false);

        setTitle(result.getOperation());
        setSize(200, 100);

        this.panel = new BorderLayoutPanel();

        List<TreeTableColumnDefinition<OperationResult, Object>> columns = new ArrayList<>();
        columns.add(new TreeTableColumnDefinition<>("Operation", 150,
                r -> r.getOperation().replace("com.evolveum.midpoint", "..")));
        columns.add(new TreeTableColumnDefinition<OperationResult, Object>("Status", 50,
                r -> String.valueOf(r.getStatus()))
                .tableCellRenderer(createStatusTableCellRenderer()));
        columns.add(new TreeTableColumnDefinition<>("Message", 500,
                r -> r.getMessage() != null ? r.getMessage() : ""));
        columns.add(new TreeTableColumnDefinition<>("Context", 150,
                r -> {

                    Map<String, Collection<String>> ctx = r.getContext();

                    StringBuilder sb = new StringBuilder();
                    for (String key : ctx.keySet()) {
                        sb.append(key).append(":").append(StringUtils.join(ctx.get(key), ',')).append('\n');
                    }
                    return sb.toString();
                }));

        JXTreeTable table = MidPointUtils.createTable(new OperationResultModel(result, columns), (List) columns);
        table.setRootVisible(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JComponent toolbar = initToolbar(table, result);
        this.panel.addToTop(toolbar);
        this.panel.addToCenter(new JBScrollPane(table));

        init();
    }

    private TableCellRenderer createStatusTableCellRenderer() {
        return new ColoredTableCellRenderer() {

            @Override
            protected void customizeCellRenderer(JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
                append(value != null ? value.toString() : "");

                if (!(value instanceof OperationResultStatus)) {
                    return;
                }

                OperationResultStatus status = (OperationResultStatus) value;
                switch (status) {
                    case SUCCESS:
                    case HANDLED_ERROR:
                        setForeground(JBColor.GREEN.darker());
                        break;
                    case PARTIAL_ERROR:
                    case FATAL_ERROR:
                        setForeground(JBColor.RED.darker());
                        break;
                    case IN_PROGRESS:
                        setForeground(JBColor.BLUE.darker());
                        break;
                    case NOT_APPLICABLE:
                    case UNKNOWN:
                    case WARNING:
                        setForeground(JBColor.ORANGE.darker());
                        break;

                }
            }
        };

//        return new DefaultTableCellRenderer() {
//
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//                if (!(value instanceof OperationResultStatus)) {
//                    return c;
//                }
//
//                OperationResultStatus status = (OperationResultStatus) value;
//                switch (status) {
//                    case SUCCESS:
//                    case HANDLED_ERROR:
//                        c.setForeground(JBColor.GREEN.darker());
//                        break;
//                    case PARTIAL_ERROR:
//                    case FATAL_ERROR:
//                        c.setForeground(JBColor.RED.darker());
//                        break;
//                    case IN_PROGRESS:
//                        c.setForeground(JBColor.BLUE.darker());
//                        break;
//                    case NOT_APPLICABLE:
//                    case UNKNOWN:
//                    case WARNING:
//                        c.setForeground(JBColor.ORANGE.darker());
//                        break;
//
//                }
//
//                return c;
//            }
//        };
    }

    private JComponent initToolbar(JXTreeTable table, OperationResult result) {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> table.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> table.collapseAll());
        group.add(collapseAll);

        group.add(new Separator());

        AnAction export = MidPointUtils.createAnAction("Export Result", AllIcons.Actions.Download, e -> openSaveResultDialog(e, result));
        group.add(export);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("OperationResultDialogToolbar", group, true);
        return toolbar.getComponent();
    }

    private void openSaveResultDialog(AnActionEvent e, OperationResult result) {
        Project project = e.getProject();
        String basePath = project.getBasePath();

        VirtualFile projectRoot = LocalFileSystem.getInstance().findFileByPath(basePath);

        FileSaverDialog saver = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save Operation Result As Xml", "Save to", "xml"), project);

        VirtualFileWrapper target = saver.save(null, project.getName() + ".xml");
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

    private void saveResult(final Project project, ProgressIndicator indicator, final VirtualFileWrapper fileWrapper, OperationResult result) {
        MidPointManager mm = MidPointManager.getInstance(project);

        EnvironmentManager em = EnvironmentManager.getInstance(project);
        Environment environment = em.getSelected();

        RunnableUtils.runWriteActionAndWait(() -> {
            File file = fileWrapper.getFile();

            try {
                if (file.exists()) {
                    file.delete();
                }

                file.createNewFile();
            } catch (IOException ex) {
                mm.printToConsole(OperationResultDialog.class, "Couldn't create file " + file.getPath() + " for operation result", ex);
            }

            VirtualFile vFile = fileWrapper.getVirtualFile();

            try (BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(vFile.getOutputStream(this), vFile.getCharset()))) {

                MidPointClient client = new MidPointClient(project, environment);

                PrismContext ctx = client.getPrismContext();
                PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);

                String xml = serializer.serializeAnyData(result.createOperationResultType(), SchemaConstantsGenerated.C_OPERATION_RESULT);

                IOUtils.write(xml, out);
            } catch (IOException | SchemaException ex) {
                mm.printToConsole(OperationResultDialog.class, "Couldn't create file " + file.getPath() + " for operation result", ex);
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
