package com.evolveum.midpoint.studio.ui.smart.suggestion.listener;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.EnvironmentSettings;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * The smart suggestion balloon must stay quiet unless the project has the MidPoint facet,
 * an environment is selected, the user has not hidden it and the file is a resource.
 *
 * Each test gets its own light project because some tests add the facet to the module.
 */
public class EditorFileOpenListenerTest extends BasePlatformTestCase {

    private static final String RESOURCE_XML = """
            <resource xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                      oid="11111111-1111-1111-1111-111111111111">
                <name>test</name>
            </resource>
            """;

    private static final String USER_XML = """
            <user xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <name>administrator</name>
            </user>
            """;

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    public void testResourceWithFacetAndEnvironment() {
        addMidPointFacet();
        selectEnvironment();
        VirtualFile file = addTextFile("objects/resource.xml", RESOURCE_XML);

        assertTrue(EditorFileOpenListener.shouldShowNotification(getProject(), file));
    }

    public void testWithoutFacet() {
        selectEnvironment();
        VirtualFile file = addTextFile("objects/resource.xml", RESOURCE_XML);

        assertFalse(EditorFileOpenListener.shouldShowNotification(getProject(), file));
    }

    public void testWithoutSelectedEnvironment() {
        addMidPointFacet();
        clearSelectedEnvironment();
        VirtualFile file = addTextFile("objects/resource.xml", RESOURCE_XML);

        assertFalse(EditorFileOpenListener.shouldShowNotification(getProject(), file));
    }

    public void testHiddenByUser() {
        addMidPointFacet();
        selectEnvironment();
        VirtualFile file = addTextFile("objects/resource.xml", RESOURCE_XML);

        EditorFileOpenListener.hideNotification(getProject());

        assertFalse(EditorFileOpenListener.shouldShowNotification(getProject(), file));
        assertTrue(MidPointService.get(getProject()).getSettings().isHideSmartSuggestionNotification());
    }

    public void testNonResourceFile() {
        addMidPointFacet();
        selectEnvironment();
        VirtualFile file = addTextFile("objects/user.xml", USER_XML);

        assertFalse(EditorFileOpenListener.shouldShowNotification(getProject(), file));
    }

    private VirtualFile addTextFile(String path, String text) {
        return myFixture.addFileToProject(path, text).getVirtualFile();
    }

    private void addMidPointFacet() {
        FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        WriteAction.run(() -> FacetUtil.addFacet(getModule(), facetType, MidPointFacetType.FACET_NAME));
    }

    private void clearSelectedEnvironment() {
        EnvironmentService.getInstance(getProject()).getSettings().setSelectedId(null);
    }

    private void selectEnvironment() {
        Environment env = new Environment("test-env");
        env.setName("test");
        env.setUrl("http://localhost:8080/midpoint");

        EnvironmentSettings settings = EnvironmentService.getInstance(getProject()).getSettings();
        settings.getEnvironments().add(env);
        settings.setSelectedId(env.getId());
    }
}
