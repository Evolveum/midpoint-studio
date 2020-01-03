package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.evolveum.midpoint.studio.ui.metrics.MetricsEditorProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.intellij.openapi.project.Project;
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

    private Project project;

    private List<Node> nodes = new ArrayList<>();

    private Map<MetricsKey, List<DataPoint>> dataPoints = new HashMap<>();

    public InMemoryMetricsSession(@NotNull UUID id, @NotNull Environment environment, @NotNull Project project) {
        this.id = id;
        this.environment = environment;
        this.project = project;
    }

    public void init() {
        try {
            MidPointManager mm = MidPointManager.getInstance(project);
            Service client = MidPointUtils.buildRestClient(environment, mm);

            List<NodeType> nodes = client.search(NodeType.class).list();
            System.out.println(nodes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
