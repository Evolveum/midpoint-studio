package com.evolveum.midpoint.studio.impl.lang.prism.validator;

import com.evolveum.concepts.ValidationLog;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The external annotator is registered for every XML, JSON and YAML file in every project.
 * It must not touch the project prism context in a project without the MidPoint facet or
 * for a file that is not a midPoint object file.
 *
 * Each test gets its own light project (fresh descriptor instance), so "service not created"
 * is a meaningful assertion regardless of test order.
 */
public class PrismExternalAnnotatorTest extends BasePlatformTestCase {

    private static final String POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>example</artifactId>
                <version>1.0</version>
            </project>
            """;

    private static final String USER_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <user xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                  oid="00000000-0000-0000-0000-000000000002">
                <name>administrator</name>
            </user>
            """;

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    public void testMidpointXmlWithoutFacetIsSkipped() {
        PsiFile file = myFixture.configureByText("administrator.xml", USER_XML);

        assertNull(collectInformation(file));
        assertNull("project without facet must not build project prism context",
                getProject().getServiceIfCreated(StudioPrismContextService.class));
    }

    public void testNonMidpointXmlWithFacetIsSkipped() {
        addMidPointFacet();
        PsiFile file = myFixture.configureByText("pom.xml", POM_XML);

        assertNull(collectInformation(file));
    }

    public void testMidpointXmlWithFacetIsCollected() {
        addMidPointFacet();
        PsiFile file = myFixture.configureByText("administrator.xml", USER_XML);

        assertNotNull(collectInformation(file));
    }

    /**
     * doAnnotate runs on a platform thread that has no project bound. Prism internals resolve
     * type classes through the static PrismContext.get() during the parse, which must reach
     * the project context instead of falling back to the default one.
     */
    public void testDoAnnotateBindsProjectContext() {
        addMidPointFacet();
        PsiFile file = myFixture.configureByText("administrator.xml", USER_XML);
        PrismExternalAnnotator.PrismAnnotationInfo info = collectInformation(file);
        assertNotNull(info);

        StudioPrismContextService.resetDefaultContextFallbackCount();

        List<ValidationLog> result = new PrismExternalAnnotator().doAnnotate(info);

        assertNotNull(result);
        assertEquals("parse fell back to the default prism context",
                0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    private PrismExternalAnnotator.PrismAnnotationInfo collectInformation(PsiFile file) {
        return new PrismExternalAnnotator().collectInformation(file, myFixture.getEditor(), false);
    }

    private void addMidPointFacet() {
        FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        WriteAction.run(() -> FacetUtil.addFacet(getModule(), facetType, MidPointFacetType.FACET_NAME));
    }
}
