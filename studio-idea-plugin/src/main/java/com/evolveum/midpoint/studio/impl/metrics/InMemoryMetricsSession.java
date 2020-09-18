package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.midpoint.prism.query.QueryFactory;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.client.Service;
import com.evolveum.midpoint.studio.ui.metrics.MetricsEditorProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryMetricsSession implements MetricsSession, Disposable {

    private Project project;

    private UUID id;

    private Environment environment;

    private List<String> urls;

    private List<Node> nodes = new ArrayList<>();

    private RefreshInterval interval;

    private Map<MetricsKey, List<DataPoint>> dataPoints = new HashMap<>();

    private Map<MetricsWorker, Future> workers = new HashMap();

    private MetricsSessionListener listener;

    public InMemoryMetricsSession(@NotNull Project project, @NotNull UUID id, @NotNull Environment environment, List<String> urls) {
        this.project = project;

        this.id = id;
        this.environment = environment;
        this.urls = urls;
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
        RunnableUtils.executeWithPluginClassloader(() -> {
            try {
                MidPointClient client = new MidPointClient(project, environment);
                Service service = client.getClient();

                List<Node> nodes = urls == null || urls.isEmpty() ? setupNodeListFromEnvironment(service) : setupNodeListFromUrls(service);

                List<MetricsWorker> workers = new ArrayList<>();

                for (Node node : nodes) {
                    node.setColor(MidPointUtils.generateAwtColor());
                    this.nodes.add(node);

                    workers.add(new MetricsWorker(this, project, node));
                }

                if (listener != null) {
                    listener.nodesChanged();
                }

                for (MetricsWorker worker : workers) {
                    Future future = ApplicationManager.getApplication().executeOnPooledThread(worker);
                    this.workers.put(worker, future);
                }
            } catch (Exception ex) {
                // todo proper error handling
                throw new RuntimeException(ex);
            }

            return null;
        });
    }

    private List<Node> setupNodeListFromUrls(Service client) {
        List<Node> result = new ArrayList<>();

        int i = 0;
        for (String url : urls) {
            Node n = new Node(i, UUID.randomUUID().toString(), url, url);
            result.add(n);
        }

        return result;
    }

    private List<Node> setupNodeListFromEnvironment(Service client) {
        List<Node> result = new ArrayList<>();

        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        options.add(SelectorOptions.create(GetOperationOptions.createRaw()));

        List<NodeType> nodes;
        try {
            nodes = client.list(NodeType.class, buildNodesQuery(client), options);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        int i = 0;
        for (NodeType node : nodes) {
            Node n = new Node(i, node.getOid(), node.getName().getOrig(), node.getUrl());
            result.add(n);
        }

        return result;
    }

    private ObjectQuery buildNodesQuery(Service service) {
        PrismContext ctx = service.prismContext();
        QueryFactory qf = ctx.queryFactory();

        ItemPath path = ctx.path(ObjectType.F_NAME);
        ObjectPaging paging = qf.createPaging(0, 100, path, OrderDirection.ASCENDING);

        return qf.createQuery(null, paging);
    }

    @Override
    public void stop() {
        for (MetricsWorker worker : workers.keySet()) {
            worker.setStop(true);
        }

        try {
            workers.values().forEach(f -> f.cancel(true));
        } catch (Exception ex) {
            // todo proper error handling
            throw new RuntimeException(ex);
        }
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

    @Override
    public MetricsSessionListener getListener() {
        return listener;
    }

    @Override
    public void setListener(MetricsSessionListener listener) {
        this.listener = listener;
    }
}
