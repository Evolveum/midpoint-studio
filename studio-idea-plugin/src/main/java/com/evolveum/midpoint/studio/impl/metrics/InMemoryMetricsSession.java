package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.ui.metrics.MetricsEditorProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryMetricsSession implements MetricsSession, Disposable {

    private UUID id;

    private Environment environment;

    private List<Node> nodes = new ArrayList<>();

    private Map<MetricsKey, List<DataPoint>> dataPoints = new HashMap<>();

    public InMemoryMetricsSession(@NotNull UUID id, @NotNull Environment environment) {
        this.id = id;
        this.environment = environment;
    }

    public void init() {
        // todo
        // 1/ setup rest client
        // 2/ fetch all node objects
        // 3/ setup rest clients for all nodes
        // 4/ setup workers, prepare them for scheduling
    }

    public void dispose() {

    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public List<DataPoint> listDataPoints(@NotNull Date from, @NotNull Date to, @NotNull MetricsKey key) {
        List<DataPoint> result = new ArrayList<>();

        List<DataPoint> points = dataPoints.get(key);
        for (DataPoint point : points) {
            long time = point.getTimestamp().getTime();
            if (time < from.getTime()) {
                continue;
            }

            if (time > to.getTime()) {
                break;
            }

            result.add(point);
        }

        return result;
    }

    @Override
    public List<Node> listNodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public VirtualFile getFile() {
        return new LightVirtualFile(id.toString() + "." + MetricsEditorProvider.METRICS_FILE_EXTENSION);
    }
}
