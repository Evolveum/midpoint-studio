package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MetricsSession {

    UUID getId();

    Environment getEnvironment();

    void start();

    void stop();

    List<DataPoint> listDataPoints(Date from, Date to, MetricsKey key);

    List<Node> listNodes();

    VirtualFile getFile();

    void setListener(MetricsSessionListener listener);

    MetricsSessionListener getListener();
}
