package com.evolveum.midpoint.studio.ui;

import com.intellij.ui.AddEditRemovePanel;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentSettings;
import com.evolveum.midpoint.studio.util.Selectable;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentsPanel extends AddEditRemovePanel<Selectable<Environment>> {

    public EnvironmentsPanel(EnvironmentSettings settings) {
        super(new EnvironmentsModel(), new ArrayList<>(), null);

        setPreferredSize(new Dimension(150, 100));
        getTable().setShowColumns(true);

        initData(settings);
    }

    private void initData(EnvironmentSettings settings) {
        List<Selectable<Environment>> selectables = new ArrayList<>();

        Environment selected = settings.getSelected();

        settings.getEnvironments().forEach(e -> {
            Selectable s = new Selectable(e);
            if (selected != null) {
                s.setSelected(Objects.equals(e.getId(), selected.getId()));
            }
            selectables.add(s);
        });

        setData(selectables);
    }

    @Override
    protected boolean removeItem(Selectable<Environment> environment) {
        return getData().remove(environment);
    }

    @Nullable
    @Override
    protected Selectable<Environment> addItem() {
        return doAddOrEdit(null);
    }

    @Nullable
    @Override
    protected Selectable<Environment> editItem(Selectable<Environment> environment) {
        return doAddOrEdit(environment);
    }

    @Nullable
    private Selectable<Environment> doAddOrEdit(@Nullable Selectable<Environment> environment) {
        EnvironmentEditorDialog dialog = new EnvironmentEditorDialog(environment);
        if (!dialog.showAndGet()) {
            return null;
        }

        Selectable<Environment> env = dialog.getEnvironment();
        if (!env.isSelected()) {
            return env;
        }

        for (Selectable<Environment> e : getData()) {
            if (e.equals(env)) {
                continue;
            }

            e.setSelected(false);
        }

        return env;
    }

    public EnvironmentSettings getFullSettings() {
        EnvironmentSettings settings = new EnvironmentSettings();

        List<Environment> envts = new ArrayList<>();
        getData().forEach(s -> envts.add(s.getObject()));
        settings.setEnvironments(envts);

        for (Selectable<Environment> e : getData()) {
            if (e.isSelected()) {
                settings.setSelectedId(e.getObject().getId());
                break;
            }
        }

        return settings;
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
            switch (i) {
                case 0:
                    return environment.getName();
                case 1:
                    return environment.getUrl();
                case 2:
                    return selectable.isSelected();
                default:
                    return null;
            }
        }
    }
}
