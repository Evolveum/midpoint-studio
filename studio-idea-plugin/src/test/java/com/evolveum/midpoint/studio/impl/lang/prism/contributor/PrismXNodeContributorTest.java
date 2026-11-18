package com.evolveum.midpoint.studio.impl.lang.prism.contributor;

import com.evolveum.midpoint.studio.StudioPlatformTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * The contributor is registered for every XML, JSON and YAML file. It must stay away from the
 * project prism context for files that are not midPoint objects, and it must not fail on
 * namespace prefixed ancestors of the caret.
 */
public class PrismXNodeContributorTest extends StudioPlatformTestCase {

    private static final String POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <caret>
            </project>
            """;

    private static final String ROLE_XML = """
            <role xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                  xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3">
                <name>r</name>
                <assignment>
                    <targetRef>
                        <filter>
                            <q:text>name = "x"<caret></q:text>
                        </filter>
                    </targetRef>
                </assignment>
            </role>
            """;

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    public void testNonMidpointRootIsSkipped() {
        assertFalse(PrismXNodeContributor.appliesTo(myFixture.configureByText("pom.xml", POM_XML)));
    }

    public void testMidpointRootApplies() {
        assertTrue(PrismXNodeContributor.appliesTo(myFixture.configureByText("role.xml", ROLE_XML)));
    }

    public void testFileWithoutRootApplies() {
        assertTrue(PrismXNodeContributor.appliesTo(myFixture.configureByText("new.xml", "<caret>")));
        assertTrue(PrismXNodeContributor.appliesTo(myFixture.configureByText("new.json", "<caret>")));
        assertTrue(PrismXNodeContributor.appliesTo(myFixture.configureByText("new.yaml", "<caret>")));
    }

    public void testEmptyXmlOffersObjectRootElements() {
        addMidPointFacet();
        myFixture.configureByText("new.xml", "<caret>");

        myFixture.completeBasic();

        assertNotNull(myFixture.getLookupElementStrings());
        assertTrue(myFixture.getLookupElementStrings().contains("role"));
    }

    public void testCompletionUnderPrefixedAncestorDoesNotFail() {
        addMidPointFacet();
        myFixture.configureByText("role.xml", ROLE_XML);

        myFixture.completeBasic();
    }
}
