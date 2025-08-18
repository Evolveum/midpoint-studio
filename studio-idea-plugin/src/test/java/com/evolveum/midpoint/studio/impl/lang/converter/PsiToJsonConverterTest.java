package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToJsonConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiConverter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingType;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class PsiToJsonConverterTest extends BasePlatformTestCase {
    PsiFile psiFile;
    PrismContext prismContext;
    ParsingContext parsingCtx;
    PsiConverter psiConverterImpl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        psiConverterImpl = new PsiToJsonConverter();

        psiFile = myFixture.configureByText("test.json", """
                {
                    "mapping": {
                        "@oid": "10000000-0000-0000-0000-000000000004",
                        "name": "givenName-familyName-to-cn",
                        "source": [
                            {
                                "path": "$focus/givenName"
                            },
                            {
                                "path": "$focus/familyName"
                            }
                        ],
                        "expression": {
                            "script": {
                                "code": "givenName + ' ' + familyName"
                            }
                        },
                        "target": {
                            "path": "$projection/attributes/cn"
                        }
                    }
                }
                """);

        prismContext = StudioPrismContextService.getPrismContext(myFixture.getProject());
        parsingCtx = prismContext.createParsingContextForCompatibilityMode();
    }

    public void testPsiFile() {
        assertNotNull(psiFile);
        JsonFile xmlFile = (JsonFile) psiFile;
        JsonObject obj = (JsonObject) xmlFile.getTopLevelValue();
        assertNotNull(obj);
    }

    public void testRootXNode() throws SchemaException {
        String rawElement = psiConverterImpl.convert(psiFile.getFirstChild(), false);

        XNode mapping = prismContext.parserFor(rawElement)
                .language("json")
                .context(parsingCtx)
                .parseToXNode().toMapXNode().get(new QName("mapping"));

        assertNotNull(mapping);
        assertEquals(MappingType.class, mapping.getDefinition().getTypeClass());
    }
}
