package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToXmlConverter;
import com.intellij.psi.xml.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.psi.PsiFile;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class PsiToXmlConverterTest extends BasePlatformTestCase {

    private XNodeFactory xNodeFactory;
    private MapXNode mapping;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xNodeFactory  = new XNodeFactoryImpl();
        PsiConverter psiConverterImpl = new PsiToXmlConverter();
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
                    <target>
                        <path>$projection/attributes/cn</path>
                    </target>
                </mapping>
                """);

        assertNotNull(psiFile);
        XmlFile xmlFile = (XmlFile) psiFile;
        String rawCode = psiConverterImpl.convert(xmlFile.getRootTag(), true);

        PrismContext prismContext = StudioPrismContextService.getPrismContext(myFixture.getProject());
        ParsingContext parsingCtx = prismContext.createParsingContextForCompatibilityMode();
        MapXNode root = prismContext.parserFor(rawCode)
                .language("xml")
                .context(parsingCtx)
                .parseToXNode().toMapXNode();

        mapping = (MapXNode) root.get(new QName("mapping"));
    }

    public void testRootXNode() {
        assertTrue(mapping instanceof MapXNodeImpl);
    }

    public void testMapXNode() {
        MapXNode expression = (MapXNode) mapping.get(new QName("expression"));
        assertNotNull(expression);

        MapXNode script = (MapXNode) expression.get(new QName("script"));
        assertNotNull(script);

        PrimitiveXNode<String> code = (PrimitiveXNode<String>) script.get(new QName("code"));
        assertNotNull(code);
        assertEquals("givenName + ' ' + familyName", code.getStringValue());
    }

    public void testXNode() {
        XNode pathOfSource0 = ((ListXNode) mapping.get(new QName("source"))).get(0);
        XNode pathOfSource1 = ((ListXNode) mapping.get(new QName("source"))).get(1);

        assertNotNull(pathOfSource0);
        assertNotNull(pathOfSource1);

        assertEquals(pathOfSource0, xNodeFactory.map(new QName("path"), xNodeFactory.primitive("$focus/givenName")));
        assertEquals(pathOfSource1, xNodeFactory.map(new QName("path"), xNodeFactory.primitive("$focus/familyName")));
    }

    public void testPrimitiveXNode() {
        PrimitiveXNode<?> name = (PrimitiveXNode<?>) mapping.get(new QName("name"));
        assertNotNull(name);
        assertEquals("givenName-familyName-to-cn", name.getStringValue());
    }
}
