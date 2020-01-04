package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.intellij.openapi.project.Project;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsWorker implements Runnable {

    private MetricsSession session;

    private Project project;

    private NodeType node;

    private boolean stop;

    private Service<WebClient> service;

    public MetricsWorker(MetricsSession session, Project project, NodeType node) {
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

                    service = MidPointUtils.buildRestClient(env, MidPointManager.getInstance(project));
                }

                WebClient client = service.getClient();
// todo continue :)
//                client = client.path("/")
            } catch (Exception ex) {
                // todo handle exception
                throw new RuntimeException(ex);
            }
        }
    }
}
