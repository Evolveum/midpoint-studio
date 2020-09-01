package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsServiceImpl implements MetricsService, Disposable {

    private Project project;

    private Map<UUID, MetricsSession> sessions = new HashMap<>();

    public MetricsServiceImpl(@NotNull Project project) {
        this.project = project;

        init();
    }

    private void init() {
        // todo implement
    }

    @Override
    public void dispose() {
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
    public MetricsSession createSession(Environment environment) {
        return createSession(environment, null);
    }

    @Override
    public MetricsSession createSession(Environment environment, List<String> urls) {
        MetricsSession session = new InMemoryMetricsSession(project, UUID.randomUUID(), environment, urls);
        sessions.put(session.getId(), session);

        return session;
    }

    @Override
    public MetricsSession loadSession(VirtualFile file) {
        // todo implement
        return null;
    }
}
