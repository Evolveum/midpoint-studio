package com.evolveum.midpoint.studio.impl.ide;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Properties;

/**
 * Checks that file templates used by new project wizard resolve both through IDE
 * registry and directly from bundled resources.
 */
public class MidPointFileTemplatesTest extends BasePlatformTestCase {

    public void testTemplatesRegisteredInIde() {
        FileTemplateManager manager = FileTemplateManager.getInstance(getProject());

        assertNotNull(manager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_MAVEN_POM_TEMPLATE));
        assertNotNull(manager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_GIT_IGNORE_TEMPLATE));
    }

    public void testMavenPomText() throws Exception {
        String registered = MidPointFileTemplates.text(getProject(), MidPointFileTemplateGroupFactory.MIDPOINT_MAVEN_POM_TEMPLATE, properties());
        String bundled = MidPointFileTemplates.bundledText(MidPointFileTemplateGroupFactory.MIDPOINT_MAVEN_POM_TEMPLATE, properties());

        assertTrue(bundled.contains("<artifactId>sample-project</artifactId>"));
        assertTrue(bundled.contains("<midpoint.version>4.11</midpoint.version>"));
        assertFalse(bundled.contains("${GROUP_ID}"));
        assertEquals(registered, bundled);
    }

    public void testGitIgnoreText() throws Exception {
        String registered = MidPointFileTemplates.text(getProject(), MidPointFileTemplateGroupFactory.MIDPOINT_GIT_IGNORE_TEMPLATE, null);
        String bundled = MidPointFileTemplates.bundledText(MidPointFileTemplateGroupFactory.MIDPOINT_GIT_IGNORE_TEMPLATE, null);

        assertFalse(bundled.isBlank());
        assertEquals(registered, bundled);
    }

    public void testUnknownBundledTemplate() {
        try {
            MidPointFileTemplates.bundledText("Unknown.xml", null);
            fail("Expected IOException");
        } catch (java.io.IOException ex) {
            assertTrue(ex.getMessage().contains("Unknown.xml"));
        }
    }

    private Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("GROUP_ID", "sample-project");
        properties.setProperty("ARTIFACT_ID", "sample-project");
        properties.setProperty("VERSION", "1.0-SNAPSHOT");
        properties.setProperty("PROJECT_NAME", "sample-project");
        properties.setProperty("MIDPOINT_VERSION", "4.11");
        return properties;
    }
}
