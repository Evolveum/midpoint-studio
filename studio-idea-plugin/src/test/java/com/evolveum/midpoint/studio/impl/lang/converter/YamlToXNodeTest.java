package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.yaml.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class YamlToXNodeTest extends BasePlatformTestCase {
    private XNodeFactory xNodeFactory;
    private MapXNode yamlRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        xNodeFactory  = new XNodeFactoryImpl();
        XNodeConverter xNodeConverterImpl = new YamlToXNode();
        PsiFile psiFile = myFixture.configureByText("test.yaml", """
                    objectType: UserType
                    name: jdoe
                    fullName:
                      orig: John Doe
                    givenName:
                      orig: John
                    familyName:
                      orig: Doe
                    emailAddress: john.doe@example.com
                    activation:
                      administrativeStatus: enabled
                    assignment:
                      - targetRef:
                          oid: 12345678-9abc-def0-1234-56789abcdef0
                          type: RoleType
                """);

        assertNotNull(psiFile);
        YAMLFile yamlFile = (YAMLFile) psiFile;
        YAMLDocument doc = yamlFile.getDocuments().get(0);
        YAMLMapping root = (YAMLMapping) doc.getTopLevelValue();
        assertNotNull(root);

        yamlRoot = (MapXNode) xNodeConverterImpl.convertFromPsi(root);
        assertNotNull(yamlRoot);
        assertTrue(yamlRoot instanceof MapXNodeImpl);
    }

    public void testMapXNode() {
        MapXNode assignment = (MapXNode) ((ListXNode) yamlRoot.get(new QName("assignment"))).get(0);
        assertNotNull(assignment);

        MapXNode targetRef = (MapXNode) assignment.get(new QName("targetRef"));
        assertNotNull(targetRef);

        PrimitiveXNode<?> oid = (PrimitiveXNode<?>) targetRef.get(new QName("oid"));
        assertNotNull(oid);
        assertEquals("12345678-9abc-def0-1234-56789abcdef0", oid.getValue());

        PrimitiveXNode<?> type = (PrimitiveXNode<?>) targetRef.get(new QName("type"));
        assertNotNull(type);
        assertEquals("RoleType", type.getValue());
    }

    public void testXNode() {
        MapXNode fullName = (MapXNode) yamlRoot.get(new QName("fullName"));
        assertNotNull(fullName);

        PrimitiveXNode<?> orig = (PrimitiveXNode<?>) fullName.get(new QName("orig"));
        assertNotNull(orig);
        assertEquals(orig, xNodeFactory.primitive("John Doe"));
    }


    public void testPrimitiveXNode() {
        PrimitiveXNode<?> xNode = (PrimitiveXNode<?>) yamlRoot.get(new QName("objectType"));
        assertNotNull(xNode);
        assertEquals("UserType", xNode.getValue());
    }
}
