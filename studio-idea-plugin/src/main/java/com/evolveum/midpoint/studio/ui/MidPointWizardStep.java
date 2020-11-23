package com.evolveum.midpoint.studio.ui;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.evolveum.midpoint.studio.impl.ProjectSettings;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointWizardStep extends ModuleWizardStep {

    private ProjectConfigurationPanel panel;

    public MidPointWizardStep(ProjectSettings settings) {
        super();

        panel = new ProjectConfigurationPanel(settings, false);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return panel.validateData();
    }

    @Override
    public void updateDataModel() {
        panel.updateSettings();
    }
}
