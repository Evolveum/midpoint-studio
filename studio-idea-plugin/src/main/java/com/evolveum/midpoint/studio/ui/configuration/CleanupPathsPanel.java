package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.impl.configuration.CleanupPath;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CleanupPathsPanel extends AddEditRemovePanel<CleanupPath> {

    private final Project project;

    public CleanupPathsPanel(@NotNull Project project) {
        super(new CleanupPathsModel(), new ArrayList<>(), null);

        this.project = project;
    }

    @Override
    protected @Nullable CleanupPath addItem() {
        return doAddOrEdit(null);
    }

    @Override
    protected boolean removeItem(CleanupPath item) {
        return true;
    }

    @Override
    protected @Nullable CleanupPath editItem(CleanupPath o) {
        return doAddOrEdit(o);
    }

    @Nullable
    private CleanupPath doAddOrEdit(@Nullable CleanupPath data) {
        CleanupPathDialog dialog = new CleanupPathDialog(project, data);
        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog.getData();
    }
    private static class CleanupPathsModel extends AddEditRemovePanel.TableModel<CleanupPath> {

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
        public Object getField(CleanupPath item, int column) {
            return switch (column) {
                case 0 -> item.getType();
                case 1 -> item.getPath();
                case 2 -> item.getAction();
                default -> null;
            };
        }
    }
}
