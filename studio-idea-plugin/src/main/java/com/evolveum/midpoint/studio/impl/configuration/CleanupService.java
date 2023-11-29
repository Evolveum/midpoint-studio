package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.impl.ServiceBase;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(
        name = "CleanupService", storages = @Storage(value = "midpoint.xml")
)
public class CleanupService extends ServiceBase<CleanupConfiguration> {

    public CleanupService(@NotNull Project project) {
        super(project, CleanupConfiguration.class);
    }

    public static CleanupService getInstance(@NotNull Project project) {
        return project.getService(CleanupService.class);
    }

    @Override
    protected CleanupConfiguration createDefaultSettings() {
        return new CleanupConfiguration();
    }
}
