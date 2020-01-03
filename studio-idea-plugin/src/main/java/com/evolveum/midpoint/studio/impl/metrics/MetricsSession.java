package com.evolveum.midpoint.studio.impl.metrics;

import com.evolveum.midpoint.studio.impl.Environment;

import java.util.Date;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MetricsSession {

    Environment getEnvironment();

    void start();

    void stop();

    List<DataPoint> listDataPoints(Date from, Date to, MetricsKey key);

    List<Node> listNodes();
}
