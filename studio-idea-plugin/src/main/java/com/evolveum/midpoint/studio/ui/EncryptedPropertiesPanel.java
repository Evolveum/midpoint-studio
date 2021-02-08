package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.EncryptedPropertiesParser;
import com.evolveum.midpoint.studio.util.EncryptedPropertiesSerializer;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.AddEditRemovePanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedPropertiesPanel extends AddEditRemovePanel<EncryptedProperty> {

    private Project project;

    private EnvironmentService environmentService;

    public EncryptedPropertiesPanel(@NotNull Project project, @NotNull EnvironmentService environmentService) {
        super(new EncryptedPropertyModel(environmentService), new ArrayList<>(), null);

        this.project = project;
        this.environmentService = environmentService;

        initData();

        getTable().setShowColumns(true);
    }

    private void initData() {
        EncryptionService manager = EncryptionService.getInstance(project);
        getData().clear();
        getData().addAll(manager.list(EncryptedProperty.class));
    }

    @Nullable
    @Override
    protected EncryptedProperty addItem() {
        return doAddOrEdit(null);
    }

    @Override
    protected boolean removeItem(EncryptedProperty o) {
        EncryptionService manager = EncryptionService.getInstance(project);
        return manager.delete(o.getKey());
    }

    @Nullable
    @Override
    protected EncryptedProperty editItem(EncryptedProperty o) {
        return doAddOrEdit(o);
    }

    @Nullable
    private EncryptedProperty doAddOrEdit(EncryptedProperty property) {
        EncryptedPropertyEditorDialog dialog = new EncryptedPropertyEditorDialog(property, environmentService.getEnvironments());
        if (!dialog.showAndGet()) {
            return null;
        }

        EncryptedProperty updated = dialog.getEncryptedProperty();

        EncryptionService manager = EncryptionService.getInstance(project);
        manager.add(updated);

        return updated;
    }

    public AnAction[] createConsoleActions() {
        return new AnAction[]{
                new AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        EncryptionService manager = EncryptionService.getInstance(project);
                        manager.refresh();

                        initData();
                    }
                },
                new Separator(),
                new AnAction("Export", "Export As Properties File", AllIcons.ToolbarDecorator.Export) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        exportPerformed(e);
                    }
                },
                new AnAction("Import", "Import Properties File", AllIcons.ToolbarDecorator.Import) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        importPerformed(e);
                    }
                }
        };
    }

    private void exportPerformed(AnActionEvent evt) {
        FileSaverDialog dialog = FileChooserFactory.getInstance()
                .createSaveFileDialog(new FileSaverDescriptor("Export properties", "Export encrypted properties", "properties"), project);

        VirtualFileWrapper target = dialog.save(evt.getProject().getBaseDir(), project.getName() + ".properties");
        if (target != null) {
            Task.Backgroundable task = new Task.Backgroundable(project, "Exporting encrypted properties") {

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    RunnableUtils.runWriteActionAndWait(() -> exportProperties(target.getFile()));
                }
            };
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        }
    }

    private void exportProperties(File file) {
        EncryptionService es = EncryptionService.getInstance(project);
        try {
            List<EncryptedProperty> properties = es.list(EncryptedProperty.class);

            EncryptedPropertiesSerializer serializer = new EncryptedPropertiesSerializer(environmentService);
            serializer.serialize(properties, file);
        } catch (IOException ex) {
            MidPointService mm = MidPointService.getInstance(project);
            mm.printToConsole(environmentService.getSelected(), OperationResultDialog.class, "Couldn't create file " + file.getPath() + " for operation result", ex);
        }
    }

    private void importPerformed(AnActionEvent evt) {
        FileChooserDialog dialog = FileChooserFactory.getInstance()
                .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor("properties"), project, this);

        VirtualFile parent = project.getBaseDir();
        VirtualFile[] files = dialog.choose(project, parent);

        if (files == null || files.length == 0) {
            return;
        }

        VirtualFile file = files[0];
        Task.Backgroundable task = new Task.Backgroundable(project, "Importing encrypted properties") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RunnableUtils.runWriteActionAndWait(() -> importProperties(VfsUtil.virtualToIoFile(file)));
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void importProperties(File file) {
        EncryptionService es = EncryptionService.getInstance(project);
        try {
            EncryptedPropertiesParser parser = new EncryptedPropertiesParser(environmentService) {

                @Override
                protected String mapEnvironment(String envName) {
                    // todo ask via dialog how to map environment

                    return super.mapEnvironment(envName);
                }
            };
            List<EncryptedProperty> properties = parser.parse(file);

            properties.forEach(p -> es.add(p));
        } catch (IOException ex) {
            MidPointService mm = MidPointService.getInstance(project);
            mm.printToConsole(environmentService.getSelected(), OperationResultDialog.class, "Couldn't import file " + file.getPath() + " for operation result", ex);
        }
    }

    private static class EncryptedPropertyModel extends TableModel<EncryptedProperty> {

        private static final String[] COLUMN_NAMES = {"Key", "Environment", "Value", "Description"};

        private EnvironmentService environmentService;

        public EncryptedPropertyModel(EnvironmentService environmentService) {
            this.environmentService = environmentService;
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Nullable
        @Override
        public String getColumnName(int i) {
            return COLUMN_NAMES[i];
        }

        @Override
        public Object getField(EncryptedProperty property, int i) {
            switch (i) {
                case 0:
                    return property.getKey();
                case 1:
                    if (StringUtils.isNotEmpty(property.getEnvironment())) {
                        Environment env = environmentService.get(property.getEnvironment());
                        if (env != null) {
                            return env.getName();
                        }

                        return property.getEnvironment();
                    }

                    return EncryptedPropertyEditorDialog.ALL_ENVIRONMENTS;
                case 2:
                    if (property.getValue() == null) {
                        return null;
                    }

                    return StringUtils.abbreviate(StringUtils.repeat("*", property.getValue().length()), 15);
                case 3:
                    return property.getDescription();
                default:
                    return null;
            }
        }
    }
}
