package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.client.Service;
import com.intellij.openapi.project.Project;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsWorker implements Runnable {

    private MetricsSession session;

    private Project project;

    private Node node;

    private boolean stop;

    private Service service;

    public MetricsWorker(MetricsSession session, Project project, Node node) {
        this.session = session;
        this.project = project;
        this.node = node;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        if (stop) {
            return;
        }

        while (true) {
            if (stop) {
                break;
            }

            try {
                if (service == null) {
                    Environment env = new Environment(session.getEnvironment());
                    env.setUrl(node.getUrl());

//                    service = MidPointUtils.buildRestClient(env, MidPointManager.getInstance(project));
                }

//                WebClient client = service.getClient();
//                for (MetricsKey key : MetricsKey.values()) {
//                    WebClient c = client.path("/actuator/metrics/" + key.getKey());
//                    Map data = c.get(Map.class);
//
//                    System.out.println(data);
//                }
// todo continue :)
//                client = client.path("/")

                Thread.sleep(5000L);
            } catch (Exception ex) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                }
                // todo handle exception
                throw new RuntimeException(ex);
            }
        }
    }
}
