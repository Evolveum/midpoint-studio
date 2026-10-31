package com.evolveum.midpoint.studio.impl.psi.search;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.FileContentImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The index runs for every local xml file in every open project. Files outside the midPoint
 * namespace must be skipped before prism is touched, and a project without the MidPoint facet
 * must not get its project prism context service created because of indexing.
 *
 * Each test gets its own light project (fresh descriptor instance), so "service not created"
 * is a meaningful assertion regardless of test order.
 */
public class ObjectFileBasedIndexImplTest extends BasePlatformTestCase {

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

    public void testXmlOutsideMidpointNamespaceIsNotParsed() {
        Map<String, OidNameValue> result = index("pom.xml", POM_XML);

        assertTrue(result.isEmpty());
        assertNull("non midPoint file must not touch the project prism context",
                getProject().getServiceIfCreated(StudioPrismContextService.class));
    }

    public void testMidpointObjectIndexedWithoutFacetUsesDefaultContext() {
        Map<String, OidNameValue> result = index("administrator.xml", USER_XML);

        OidNameValue value = result.get("00000000-0000-0000-0000-000000000002");
        assertNotNull(value);
        assertEquals("administrator", value.getName());
        assertEquals(ObjectTypes.USER, value.getType());
        assertNull("project without facet must not build project prism context",
                getProject().getServiceIfCreated(StudioPrismContextService.class));
    }

    public void testMidpointObjectIndexedWithFacetUsesProjectContext() {
        addMidPointFacet();

        Map<String, OidNameValue> result = index("administrator.xml", USER_XML);

        assertNotNull(result.get("00000000-0000-0000-0000-000000000002"));
        assertNotNull("project with facet should use the project prism context",
                getProject().getServiceIfCreated(StudioPrismContextService.class));
    }

    private void addMidPointFacet() {
        FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        WriteAction.run(() -> FacetUtil.addFacet(getModule(), facetType, MidPointFacetType.FACET_NAME));
    }

    private Map<String, OidNameValue> index(String fileName, String text) {
        LightVirtualFile file = new LightVirtualFile(fileName, XmlFileType.INSTANCE, text);
        FileContent content = FileContentImpl.createByText(file, text, getProject());

        return new ObjectFileBasedIndexImpl().getIndexer().map(content);
    }
}
