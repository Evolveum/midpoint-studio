package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointProjectNotifierAdapter implements MidPointProjectNotifier {

    @Override
    public void environmentChanged(Environment oldEnv, Environment newEnv) {
    }

    @Override
    public void selectedTraceNodeChange(OpNode node) {
    }

    @Override
    public void selectedPerformanceNodeChange(OperationPerformance node) {
    }
}
