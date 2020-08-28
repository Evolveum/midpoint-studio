package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsManagerImpl implements MetricsManager, BaseComponent {

    private Project project;

    private Map<UUID, MetricsSession> sessions = new HashMap<>();

    public MetricsManagerImpl(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        // todo implement
    }

    @Override
    public void disposeComponent() {
        for (MetricsSession session : sessions.values()) {
            if (session instanceof Disposable) {
                ((Disposable) session).dispose();
            }
        }
    }

    @Override
    public MetricsSession getSession(@NotNull UUID id) {
        return sessions.get(id);
    }

    @Override
    public MetricsSession createSession() {
        EnvironmentManager envManager = EnvironmentManager.getInstance(project);
        Environment env = envManager.getSelected();

        return new InMemoryMetricsSession(UUID.randomUUID(), env, project);
    }

    @Override
    public MetricsSession loadSession(VirtualFile file) {
        // todo implement
        return null;
    }
}
