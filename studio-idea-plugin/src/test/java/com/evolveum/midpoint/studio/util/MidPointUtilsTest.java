package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.PsiTestUtils;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * Tests for MidPointUtils.
 */
public class MidPointUtilsTest extends BasePlatformTestCase {

    private static final String COMMON_NS = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";

    public void testMidpointUserXml() {
        assertTrue(isMidpointFile("user.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <user xmlns="%s">
                    <name>x</name>
                </user>
                """.formatted(COMMON_NS), XMLLanguage.INSTANCE));
    }

    public void testPomXml() {
        assertFalse(isMidpointFile("pom.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                </project>
                """, XMLLanguage.INSTANCE));
    }

    public void testJsonWithMidpointNamespace() {
        assertTrue(isMidpointFile("user.json", """
                {"user": {"@ns": "%s", "name": "x"}}
                """.formatted(COMMON_NS), JsonLanguage.INSTANCE));
    }

    public void testJsonWithUnknownElementButMidpointNamespace() {
        assertTrue(isMidpointFile("other.json", """
                {"something": {"@ns": "%s", "name": "x"}}
                """.formatted(COMMON_NS), JsonLanguage.INSTANCE));
    }

    public void testJsonWithoutNamespace() {
        assertFalse(isMidpointFile("user.json", """
                {"user": {"name": "x"}}
                """, JsonLanguage.INSTANCE));
    }

    public void testJsonWithTwoRootProperties() {
        assertFalse(isMidpointFile("two.json", """
                {"user": {"@ns": "%s"}, "role": {"@ns": "%s"}}
                """.formatted(COMMON_NS, COMMON_NS), JsonLanguage.INSTANCE));
    }

    public void testArbitraryJson() {
        assertFalse(isMidpointFile("package.json", """
                {"name": "x", "version": "1.0.0"}
                """, JsonLanguage.INSTANCE));
    }

    public void testJsonWithForeignNamespace() {
        assertFalse(isMidpointFile("other.json", """
                {"user": {"@ns": "http://example.com/ns", "name": "x"}}
                """, JsonLanguage.INSTANCE));
    }

    public void testJsonTopLevelArray() {
        assertFalse(isMidpointFile("list.json", """
                [{"user": {"name": "x"}}]
                """, JsonLanguage.INSTANCE));
    }

    public void testYamlWithMidpointNamespace() {
        assertTrue(isMidpointFile("user.yaml", """
                user:
                  "@ns": "%s"
                  name: x
                """.formatted(COMMON_NS), YAMLLanguage.INSTANCE));
    }

    public void testYamlWithUnknownElementButMidpointNamespace() {
        assertTrue(isMidpointFile("other.yaml", """
                something:
                  "@ns": "%s"
                  name: x
                """.formatted(COMMON_NS), YAMLLanguage.INSTANCE));
    }

    public void testYamlWithoutNamespace() {
        assertFalse(isMidpointFile("user.yaml", """
                user:
                  name: x
                """, YAMLLanguage.INSTANCE));
    }

    public void testYamlWithTwoRootKeys() {
        assertFalse(isMidpointFile("two.yaml", """
                user:
                  "@ns": "%s"
                role:
                  "@ns": "%s"
                """.formatted(COMMON_NS, COMMON_NS), YAMLLanguage.INSTANCE));
    }

    public void testArbitraryYaml() {
        assertFalse(isMidpointFile("config.yaml", """
                name: x
                version: 1.0.0
                """, YAMLLanguage.INSTANCE));
    }

    public void testYamlWithForeignNamespace() {
        assertFalse(isMidpointFile("other.yaml", """
                user:
                  "@ns": "http://example.com/ns"
                  name: x
                """, YAMLLanguage.INSTANCE));
    }

    public void testYamlTopLevelSequence() {
        assertFalse(isMidpointFile("list.yaml", """
                - user:
                    name: x
                """, YAMLLanguage.INSTANCE));
    }

    public void testNullFile() {
        assertFalse(MidPointUtils.isMidpointFile(null));
    }

    private boolean isMidpointFile(String fileName, String text, Language language) {
        PsiFile file = PsiTestUtils.createPsiFileFromSnippet(getProject(), fileName, text, language);
        return MidPointUtils.isMidpointFile(file);
    }
}
