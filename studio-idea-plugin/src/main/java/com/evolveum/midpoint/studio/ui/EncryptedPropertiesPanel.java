package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
                }
        };
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
