package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryMetricsSession implements MetricsSession, Disposable {

    private Environment environment;

    private List<Node> nodes = new ArrayList<>();

    private Map<MetricsKey, List<DataPoint>> dataPoints = new HashMap<>();

    public InMemoryMetricsSession(@NotNull Environment environment) {
        this.environment = environment;
    }

    public void init() {

    }

    public void dispose() {

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
}
