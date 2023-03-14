package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfigurableProvider extends ConfigurableProvider {

    private Project project;

    public MidPointConfigurableProvider(Project project) {
        this.project = project;
    }

    @Override
    public boolean canCreateConfigurable() {
        return MidPointUtils.hasMidPointFacet(project);
    }

    @Override
    public @Nullable Configurable createConfigurable() {
        return new MidPointConfigurable(project);
    }
}
