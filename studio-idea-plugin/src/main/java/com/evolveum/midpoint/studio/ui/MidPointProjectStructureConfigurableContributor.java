package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurableContributor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointProjectStructureConfigurableContributor extends ProjectStructureConfigurableContributor {

    @Override
    public @NotNull List<? extends Configurable> getExtraProjectConfigurables(@NotNull Project project, @NotNull StructureConfigurableContext context) {
        return Arrays.asList(new MidPointProjectStructureConfigurable(project, context));
    }
}
