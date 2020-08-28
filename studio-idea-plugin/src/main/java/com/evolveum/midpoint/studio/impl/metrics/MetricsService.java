package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MetricsService {

    static MetricsService getInstance(@NotNull Project project) {
        return project.getComponent(MetricsService.class);
    }

    MetricsSession createSession(Environment environment);

    MetricsSession createSession(Environment environment, List<String> urls);

    MetricsSession loadSession(VirtualFile file);

    MetricsSession getSession(UUID id);
}
