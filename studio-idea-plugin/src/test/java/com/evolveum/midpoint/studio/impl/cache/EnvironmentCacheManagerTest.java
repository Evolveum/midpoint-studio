package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.StudioPlatformTestCase;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

/**
 * The manager refreshes once after creation with whatever environment is selected, later on
 * every environment change. Without a selected environment there is nothing to connect to.
 */
public class EnvironmentCacheManagerTest extends StudioPlatformTestCase {

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    /**
     * MidPointClient refuses a null environment, and the deferred refresh runs on the EDT, so the
     * error would land in whatever happens to run there, see testWithoutSelectedEnvironment in
     * EditorFileOpenListenerTest failing on CI.
     */
    public void testNoSelectedEnvironmentSkipsConnectionAndReload() {
        EnvironmentService.getInstance(getProject()).getSettings().setSelectedId(null);

        EnvironmentCacheManager manager = EnvironmentCacheManager.get(getProject());
        // runs the refresh scheduled by the constructor
        UIUtil.dispatchAllInvocationEvents();

        manager.reload(true, true);
        manager.reloadInternal(false, true);
    }
}
