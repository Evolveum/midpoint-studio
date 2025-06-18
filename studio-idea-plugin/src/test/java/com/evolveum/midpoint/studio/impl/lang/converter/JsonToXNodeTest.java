package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class JsonToXNodeTest extends BasePlatformTestCase {
    private XNodeFactory xNodeFactory;
    private MapXNode jsonRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xNodeFactory  = new XNodeFactoryImpl();
        XNodeConverter xNodeConverterImpl = new JsonToXNode();
        PsiFile psiFile = myFixture.configureByText("test.json", """
                {
                  "objectType": "UserType",
                  "name": "jdoe",
                  "fullName": {
                    "orig": "John Doe"
                  },
                  "givenName": {
                    "orig": "John"
                  },
                  "familyName": {
                    "orig": "Doe"
                  },
                  "emailAddress": "john.doe@example.com",
                  "activation": {
                    "administrativeStatus": "enabled"
                  },
                  "assignment": [
                    {
                      "targetRef": {
                        "oid": "12345678-9abc-def0-1234-56789abcdef0",
                        "type": "RoleType"
                      }
                    }
                  ]
                }
                """);

        assertNotNull(psiFile);
        JsonFile xmlFile = (JsonFile) psiFile;
        JsonObject obj = (JsonObject) xmlFile.getTopLevelValue();
        XNode root = xNodeConverterImpl.convertFromPsi(obj);

        assertNotNull(root);
        assertTrue(root instanceof MapXNodeImpl);
        jsonRoot = (MapXNodeImpl) root;
        assertNotNull(jsonRoot);
    }

    public void testMapXNode() {
        MapXNode assignment = (MapXNode) ((ListXNode) jsonRoot.get(new QName("assignment"))).get(0);
        assertNotNull(assignment);

        MapXNode targetRef = (MapXNode) assignment.get(new QName("targetRef"));
        assertNotNull(targetRef);

        PrimitiveXNode<?> oid = (PrimitiveXNode<?>) targetRef.get(new QName("oid"));
        assertNotNull(oid);
        assertEquals("\"12345678-9abc-def0-1234-56789abcdef0\"", oid.getValue());

        PrimitiveXNode<?> type = (PrimitiveXNode<?>) targetRef.get(new QName("type"));
        assertNotNull(type);
        assertEquals("\"RoleType\"", type.getValue());
    }

    public void testXNode() {
        MapXNode fullName = (MapXNode) jsonRoot.get(new QName("fullName"));
        assertNotNull(fullName);

        PrimitiveXNode<?> orig = (PrimitiveXNode<?>) fullName.get(new QName("orig"));
        assertNotNull(orig);
        assertEquals(orig, xNodeFactory.primitive("\"John Doe\""));
    }

    public void testPrimitiveXNode() {
        PrimitiveXNode<?> xNode = (PrimitiveXNode<?>) jsonRoot.get(new QName("objectType"));
        assertNotNull(xNode);
        assertEquals("\"UserType\"", xNode.getValue());
    }
}
