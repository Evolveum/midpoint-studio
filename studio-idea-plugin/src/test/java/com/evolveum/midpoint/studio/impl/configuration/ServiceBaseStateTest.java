package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.EnvironmentServiceImpl;
import com.evolveum.midpoint.studio.impl.EnvironmentSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * The persisted project services must report no state until state was loaded from disk or a
 * caller changed settings, otherwise the platform writes .idea/midpoint.xml into every project
 * that merely instantiated them.
 *
 * Each test gets its own light project (fresh descriptor instance), so the services are
 * created fresh in every test.
 */
public class ServiceBaseStateTest extends BasePlatformTestCase {

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    public void testFreshServiceHasNoStateButHasDefaultSettings() {
        MidPointService service = MidPointService.get(getProject());

        assertNull("nothing was loaded or changed, nothing to persist", service.getState());
        assertNotNull(service.getSettings());
        assertEquals("objects/$T/$n.xml", service.getSettings().getDowloadFilePattern());
    }

    public void testNoStateLoadedKeepsStateNull() {
        MidPointService service = MidPointService.get(getProject());

        service.noStateLoaded();

        assertNull(service.getState());
        assertNotNull(service.getSettings());
    }

    public void testLoadStateMakesStateAvailable() {
        MidPointService service = MidPointService.get(getProject());

        MidPointConfiguration loaded = new MidPointConfiguration();
        loaded.setProjectId("loaded-id");
        service.loadState(loaded);

        assertNotNull(service.getState());
        assertEquals("loaded-id", service.getState().getProjectId());
        assertSame(service.getSettings(), service.getState());
    }

    public void testSetSettingsMakesStateAvailable() {
        MidPointService service = MidPointService.get(getProject());

        MidPointConfiguration settings = MidPointConfiguration.createDefaultSettings();
        service.setSettings(settings);

        assertSame(settings, service.getState());
    }

    public void testSettingsUpdatedMakesStateAvailable() {
        MidPointService service = MidPointService.get(getProject());

        service.getSettings().setAskToAddMidpointFacet(false);
        service.settingsUpdated();

        assertNotNull(service.getState());
        assertFalse(service.getState().isAskToAddMidpointFacet());
    }

    public void testCleanupServiceFollowsSameRule() {
        CleanupService service = CleanupService.get(getProject());

        assertNull(service.getState());

        service.getSettings().setRemoveMetadata(true);
        service.settingsUpdated();

        assertNotNull(service.getState());
        assertTrue(service.getState().isRemoveMetadata());
    }

    public void testProjectIdIsNullUntilRequested() {
        MidPointService service = MidPointService.get(getProject());

        assertNull(service.getSettings().getProjectId());
        assertNull(service.getState());
    }

    public void testProjectIdIsGeneratedOnceAndPersisted() {
        MidPointService service = MidPointService.get(getProject());

        String id = service.getProjectId();

        assertNotNull(id);
        assertEquals(id, service.getProjectId());
        assertEquals(id, service.getSettings().getProjectId());
        assertNotNull("generated id must be written out, otherwise the keychain entry is orphaned",
                service.getState());
        assertEquals(id, service.getState().getProjectId());
    }

    public void testProjectIdFromLoadedStateIsKept() {
        MidPointService service = MidPointService.get(getProject());

        MidPointConfiguration loaded = new MidPointConfiguration();
        loaded.setProjectId("loaded-id");
        service.loadState(loaded);

        assertEquals("loaded-id", service.getProjectId());
    }

    public void testDefaultProjectNeverReportsState() {
        // the default project is the template for new projects, its midpoint.xml entries would be
        // copied into every new project
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        MidPointService service = new MidPointService(defaultProject);

        MidPointConfiguration loaded = new MidPointConfiguration();
        loaded.setProjectId("template-id");
        service.loadState(loaded);
        assertNull("state loaded from the template must not be written back", service.getState());

        service.getSettings().setAskToAddMidpointFacet(false);
        service.settingsUpdated();
        assertNull("changes in the default project must not be written", service.getState());
    }

    public void testEnvironmentServiceFreshHasNoState() {
        EnvironmentServiceImpl service = (EnvironmentServiceImpl) EnvironmentService.getInstance(getProject());

        assertNull(service.getState());
        assertEquals(2, service.getSettings().getEnvironments().size());
    }

    public void testEnvironmentServiceLoadStateMakesStateAvailable() {
        EnvironmentServiceImpl service = (EnvironmentServiceImpl) EnvironmentService.getInstance(getProject());

        EnvironmentSettings loaded = new EnvironmentSettings();
        loaded.setSelectedId("some-id");
        service.loadState(loaded);

        assertNotNull(service.getState());
        assertEquals("some-id", service.getState().getSelectedId());
    }
}
