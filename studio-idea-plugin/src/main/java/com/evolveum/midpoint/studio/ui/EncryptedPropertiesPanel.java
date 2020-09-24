package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedPropertiesPanel extends AddEditRemovePanel<EncryptedProperty> {

    private Project project;

    public EncryptedPropertiesPanel(Project project) {
        super(new EncryptedPropertiesModel(), new ArrayList<>(), null);

        this.project = project;

        setPreferredSize(new Dimension(150, 100));
        getTable().setShowColumns(true);

        initData();
    }

    private void initData() {
        List<EncryptedProperty> properties = new ArrayList<>();

        // todo fill in

        setData(properties);
    }

    @Override
    protected boolean removeItem(EncryptedProperty environment) {
        return getData().remove(environment);
    }

    @Nullable
    @Override
    protected EncryptedProperty addItem() {
        return doAddOrEdit(null);
    }

    @Nullable
    @Override
    protected EncryptedProperty editItem(EncryptedProperty environment) {
        return doAddOrEdit(environment);
    }

    @Nullable
    private EncryptedProperty doAddOrEdit(@Nullable EncryptedProperty environment) {
//        EnvironmentEditorDialog dialog = new EnvironmentEditorDialog(project, environment);
//        if (!dialog.showAndGet()) {
//            return null;
//        }
//
//        Selectable<Environment> env = dialog.getEnvironment();
//        if (!env.isSelected()) {
//            return env;
//        }
//
//        for (Selectable<Environment> e : getData()) {
//            if (e.equals(env)) {
//                continue;
//            }
//
//            e.setSelected(false);
//        }

        return environment;
    }

//    public EnvironmentSettings getFullSettings() {
//        EnvironmentSettings settings = new EnvironmentSettings();
//
//        List<Environment> envts = new ArrayList<>();
//        getData().forEach(s -> envts.add(s.getObject()));
//        settings.setEnvironments(envts);
//
//        for (EncryptedProperty e : getData()) {
//            if (e.isSelected()) {
//                settings.setSelectedId(e.getObject().getId());
//                break;
//            }
//        }
//
//        return settings;
//    }

    private static class EncryptedPropertiesModel extends TableModel<EncryptedProperty> {

        private static final String[] COLUMN_NAMES = {"Key", "Environment", "Value", "Description"};

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
                    return property.getEnvironment();
                case 2:
                    return property.getValue();
                case 3:
                    return property.getDescription();
                default:
                    return null;
            }
        }
    }
}
