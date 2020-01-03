package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class H2MetricsSession implements MetricsSession {

    @Override
    public UUID getId() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public List<DataPoint> listDataPoints(Date from, Date to, MetricsKey key) {
        return null;
    }

    @Override
    public List<Node> listNodes() {
        return null;
    }

    @Override
    public VirtualFile getFile() {
        return null;
    }
}
