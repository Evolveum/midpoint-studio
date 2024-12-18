package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.impl.configuration.CleanupPathConfiguration;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CleanupPathsPanel extends AddEditRemovePanel<CleanupPathConfiguration> {

    private final Project project;

    public CleanupPathsPanel(@NotNull Project project) {
        super(new CleanupPathsModel(), new ArrayList<>(), null);

        this.project = project;

        getTable().setShowColumns(true);
    }

    @Override
    protected @Nullable CleanupPathConfiguration addItem() {
        return doAddOrEdit(null);
    }

    @Override
    protected boolean removeItem(CleanupPathConfiguration item) {
        return true;
    }

    @Override
    protected @Nullable CleanupPathConfiguration editItem(CleanupPathConfiguration o) {
        return doAddOrEdit(o);
    }

    @Nullable
    private CleanupPathConfiguration doAddOrEdit(@Nullable CleanupPathConfiguration data) {
        CleanupPathDialog dialog = new CleanupPathDialog(project, data);
        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog.getData();
    }

    private static class CleanupPathsModel extends TableModel<CleanupPathConfiguration> {

        private static final String[] COLUMN_NAMES = new String[]{"Type", "Path", "Action"};

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getField(CleanupPathConfiguration item, int column) {
            return switch (column) {
                case 0 -> item.getType() != null ? item.getType().getLocalPart() : null;
                case 1 -> item.getPath() != null ? item.getPath().toString() : null;
                case 2 -> StudioLocalization.get().translateEnum(item.getAction());
                default -> null;
            };
        }
    }
}
