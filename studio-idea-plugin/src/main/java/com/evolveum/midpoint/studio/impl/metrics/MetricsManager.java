package com.evolveum.midpoint.studio.impl.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MetricsManager {

    static MetricsManager getInstance(@NotNull Project project) {
        return project.getComponent(MetricsManager.class);
    }

    MetricsSession createSession();

    MetricsSession loadSession(VirtualFile file);

    MetricsSession getSession(UUID id);
}
