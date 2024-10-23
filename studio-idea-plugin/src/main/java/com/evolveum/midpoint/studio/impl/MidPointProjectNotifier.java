package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.cache.Cache;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MidPointProjectNotifier {

    Topic<MidPointProjectNotifier> MIDPOINT_NOTIFIER_TOPIC = Topic.create("MidPoint Plugin Notifications", MidPointProjectNotifier.class);

    default void environmentChanged(Environment oldEnv, Environment newEnv) {
        // intentionally empty
    }

    default void selectedTraceNodeChange(OpNode node) {
        // intentionally empty
    }

    default void selectedPerformanceNodeChange(OperationPerformance node) {
        // intentionally empty
    }

    default <C extends Cache> void environmentCacheReloaded(@NotNull EnvironmentCacheManager.CacheKey<C> key, C cache) {
        // intentionally empty
    }

    default void environmentCacheManagerReloaded() {
        // intentionally empty
    }
}
