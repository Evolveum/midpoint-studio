package com.evolveum.midpoint.studio.lang.mel;

import com.evolveum.midpoint.studio.StudioPlatformTestCase;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * End to end MEL completion: facet present, MEL injected into script/code of a resource XML,
 * completion offers MEL variables, extension namespaces and functions.
 */
public class MelCompletionTest extends StudioPlatformTestCase {

    private static final String RESOURCE_XML = """
            <resource xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <name>r</name>
                <schemaHandling>
                    <objectType>
                        <focus>
                            <type>UserType</type>
                        </focus>
                        <attribute>
                            <outbound>
                                <expression>
                                    <script>
                                        <language>%s</language>
                                        <code>%s</code>
                                    </script>
                                </expression>
                            </outbound>
                        </attribute>
                    </objectType>
                </schemaHandling>
            </resource>
            """;

    /**
     * Prefixed root element, code before language, CDATA around the code.
     */
    private static final String PREFIXED_RESOURCE_XML = """
            <c:resource xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <c:name>r</c:name>
                <c:schemaHandling>
                    <c:objectType>
                        <c:attribute>
                            <c:outbound>
                                <c:expression>
                                    <c:script>
                                        <c:code><![CDATA[%s]]></c:code>
                                        <c:language>%s</c:language>
                                    </c:script>
                                </c:expression>
                            </c:outbound>
                        </c:attribute>
                    </c:objectType>
                </c:schemaHandling>
            </c:resource>
            """;

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    private List<String> complete(String xml) {
        myFixture.configureByText("resource.xml", xml);

        myFixture.completeBasic();

        List<String> lookups = myFixture.getLookupElementStrings();
        assertNotNull("completion auto-inserted a single item or produced nothing", lookups);
        return lookups;
    }

    private List<String> complete(String language, String code) {
        addMidPointFacet();
        return complete(RESOURCE_XML.formatted(language, code));
    }

    public void testPrefixOffersVariablesAndNamespaces() {
        List<String> lookups = complete("mel", "fo<caret>");

        assertTrue("MEL variable missing in " + lookups, lookups.contains("focus"));
        assertTrue("MEL extension namespace missing in " + lookups, lookups.contains("format"));
    }

    public void testPrefixOffersGlobalFunctions() {
        List<String> lookups = complete("mel", "is<caret>");

        assertTrue("MEL global function missing in " + lookups, lookups.contains("isBlank"));
    }

    public void testLanguageUriOffersGlobalFunctions() {
        List<String> lookups = complete(
                "http://midpoint.evolveum.com/xml/ns/public/expression/language#mel", "is<caret>");

        assertTrue("MEL global function missing in " + lookups, lookups.contains("isBlank"));
    }

    public void testNamespaceDotOffersNamespaceFunctions() {
        List<String> lookups = complete("mel", "format.<caret>");

        assertTrue("MEL namespace function missing in " + lookups, lookups.contains("strftime"));
    }

    public void testMemberDotOffersMemberFunctions() {
        List<String> lookups = complete("mel", "focus.name.<caret>");

        assertTrue("MEL member function missing in " + lookups, lookups.contains("isBlank"));
        assertTrue("polystring member missing in " + lookups, lookups.contains("orig"));
    }

    public void testCdataPrefixedRootAndLanguageAfterCode() {
        addMidPointFacet();
        List<String> lookups = complete(PREFIXED_RESOURCE_XML.formatted("fo<caret>", "mel"));

        assertTrue("MEL extension namespace missing in " + lookups, lookups.contains("format"));
    }

    public void testCdataCode() {
        List<String> lookups = complete("mel", "<![CDATA[fo<caret>]]>");

        assertTrue("MEL extension namespace missing in " + lookups, lookups.contains("format"));
    }

    /**
     * MEL completion must not depend on indexes. During indexing (dumb mode) the platform skips
     * injectors and completion contributors that are not DumbAware and offers only words from
     * the document, which for a typical resource is just "focus".
     */
    public void testCompletionDuringIndexing() throws Throwable {
        addMidPointFacet();

        DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
            myFixture.configureByText("resource.xml", RESOURCE_XML.formatted("mel", "fo<caret>"));

            myFixture.completeBasic();

            List<String> lookups = myFixture.getLookupElementStrings();
            assertNotNull("single candidate auto inserted, document:\n"
                    + myFixture.getEditor().getDocument().getText(), lookups);
            assertTrue("MEL extension namespace missing in " + lookups, lookups.contains("format"));
        });
    }

    /**
     * Without the facet nothing is injected and the platform offers only words from the
     * document ("focus" from the object type definition), a single candidate that is auto
     * inserted. This is what a user sees when MEL injection does not happen.
     */
    public void testWithoutFacetOnlyDocumentWords() {
        assertOnlyFocusInserted(RESOURCE_XML.formatted("mel", "fo<caret>"));
    }

    private void assertOnlyFocusInserted(String xml) {
        myFixture.configureByText("resource.xml", xml);

        myFixture.completeBasic();

        assertNull("popup shown with " + myFixture.getLookupElementStrings(), myFixture.getLookupElementStrings());
        String text = myFixture.getEditor().getDocument().getText();
        assertTrue("single candidate not inserted:\n" + text, text.contains("<code>focus</code>"));
    }
}
