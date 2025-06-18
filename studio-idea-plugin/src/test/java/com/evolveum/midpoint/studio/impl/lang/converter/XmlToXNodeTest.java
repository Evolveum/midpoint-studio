package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.psi.xml.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.psi.PsiFile;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class XmlToXNodeTest extends BasePlatformTestCase {

    private XNodeFactory xNodeFactory;
    private MapXNode xmlRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xNodeFactory  = new XNodeFactoryImpl();
        XNodeConverter xNodeConverterImpl = new XmlToXNode();
        PsiFile psiFile = myFixture.configureByText("test.xml", """
                <mapping oid="10000000-0000-0000-0000-000000000004">
                    <name>givenName-familyName-to-cn</name>
                    <source>
                        <path>$focus/givenName</path>
                    </source>
                    <source>
                        <path>$focus/familyName</path>
                    </source>
                    <expression>
                        <script>
                            <code>givenName + ' ' + familyName</code>
                        </script>
                    </expression>
                    <condition>
                        <script>
                            <code>givenName != null &amp; familyName != null</code>
                        </script>
                    </condition>
                    <target>
                        <path>$projection/attributes/cn</path>
                    </target>
                </mapping>
                """);

        assertNotNull(psiFile);
        XmlFile xmlFile = (XmlFile) psiFile;
        XNode root = xNodeConverterImpl.convertFromPsi(xmlFile.getRootTag());

        assertTrue(root instanceof MapXNodeImpl);
        MapXNodeImpl mapXNode = (MapXNodeImpl) root;
        xmlRoot = (MapXNode) mapXNode.get(new QName("mapping"));
        assertNotNull(xmlRoot);
    }

    public void testMapXNode() {
        MapXNode expression = (MapXNode) xmlRoot.get(new QName("expression"));
        assertNotNull(expression);

        MapXNode script = (MapXNode) expression.get(new QName("script"));
        assertNotNull(script);

        PrimitiveXNode<String> code = (PrimitiveXNode<String>) script.get(new QName("code"));
        assertNotNull(code);

        assertEquals("givenName + ' ' + familyName", code.getValue());
    }

    public void testXNode() {
        XNode pathOfSource0 = ((ListXNode) xmlRoot.get(new QName("source"))).get(0);
        XNode pathOfSource1 = ((ListXNode) xmlRoot.get(new QName("source"))).get(1);

        assertNotNull(pathOfSource0);
        assertNotNull(pathOfSource1);

        assertEquals(pathOfSource0, xNodeFactory.map(new QName("path"), xNodeFactory.primitive("$focus/givenName")));
        assertEquals(pathOfSource1, xNodeFactory.map(new QName("path"), xNodeFactory.primitive("$focus/familyName")));
    }

    public void testPrimitiveXNode() {
        PrimitiveXNode<?> name = (PrimitiveXNode<?>) xmlRoot.get(new QName("name"));
        assertNotNull(name);
        assertEquals("givenName-familyName-to-cn", name.getValue());
    }
}
