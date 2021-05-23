package com.evolveum.midpoint.studio.ui.resource;

import com.intellij.ide.wizard.AbstractWizardEx;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ResourceWizard extends AbstractWizardEx {

    public ResourceWizard(@Nullable Project project, List<? extends AbstractWizardStepEx> steps) {
        super("Resource Wizard", project, steps);
    }

    @Override
    protected @NotNull JComponent createContentPane() {
        return super.createContentPane();
    }

    public static ResourceWizard createWizard(@Nullable Project project) {
        List<AbstractWizardStepEx> steps = new ArrayList<>();

        BasicStep basic = new BasicStep();
        steps.add(basic);

        ConnectorStep connector = new ConnectorStep();
        steps.add(connector);

        CapabilitiesStep capabilities = new CapabilitiesStep();
        steps.add(capabilities);

        ObjectTypesStep objectTypes = new ObjectTypesStep();
        steps.add(objectTypes);

        return new ResourceWizard(project, steps);
    }
}
