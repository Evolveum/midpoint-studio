package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.ui.EnvironmentEditorDialog;
import com.evolveum.midpoint.studio.util.Selectable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class EnvironmentsPanel extends AddEditRemovePanel<Selectable<Environment>> {

    private final Project project;

    public EnvironmentsPanel(@NotNull Project project) {
        super(new EnvironmentsModel(), new ArrayList<>(), null);

        this.project = project;

        getTable().setShowColumns(true);
    }

    @Override
    protected @Nullable Selectable<Environment> addItem() {
        return doAddOrEdit(null);
    }

    @Override
    protected boolean removeItem(Selectable<Environment> o) {
        return true;
    }

    @Override
    protected @Nullable Selectable<Environment> editItem(Selectable<Environment> o) {
        return doAddOrEdit(o);
    }

    @Nullable
    private Selectable<Environment> doAddOrEdit(@Nullable Selectable<Environment> data) {
        EnvironmentEditorDialog dialog = new EnvironmentEditorDialog(project, MidPointService.get(project).getSettings(), data);
        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog.getEnvironment();
    }

    private static class EnvironmentsModel extends TableModel<Selectable<Environment>> {

        private static final String[] COLUMN_NAMES = {"Name", "Url", "Selected"};

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
        public Object getField(Selectable<Environment> selectable, int i) {
            Environment environment = selectable.getObject();
            return switch (i) {
                case 0 -> environment.getName();
                case 1 -> environment.getUrl();
                case 2 -> selectable.isSelected();
                default -> null;
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 2 ? Boolean.class : String.class;
        }
    }
}
