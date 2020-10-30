package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.intellij.util.messages.Topic;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MidPointProjectNotifier {

    Topic<MidPointProjectNotifier> MIDPOINT_NOTIFIER_TOPIC = Topic.create("MidPoint Plugin Notifications", MidPointProjectNotifier.class);

    void environmentChanged(Environment oldEnv, Environment newEnv);

    void selectedTraceNodeChange(OpNode node);

    void selectedPerformanceNodeChange(OperationPerformance node);
}
