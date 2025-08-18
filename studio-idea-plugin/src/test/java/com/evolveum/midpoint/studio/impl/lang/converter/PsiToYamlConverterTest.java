package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToYamlConverter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingType;
import com.intellij.psi.PsiFile;
import org.jetbrains.yaml.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Created by Dominik.
 */
public class PsiToYamlConverterTest extends BasePlatformTestCase {
    PsiFile psiFile;
    PrismContext prismContext;
    ParsingContext parsingCtx;
    PsiConverter psiConverterImpl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        psiConverterImpl = new PsiToYamlConverter();
        psiFile = myFixture.configureByText("test.yaml", """
                mapping:
                  '@oid': 10000000-0000-0000-0000-000000000004
                  name: givenName-familyName-to-cn
                  source:
                    - path: $focus/givenName
                    - path: $focus/familyName
                  expression:
                    script:
                      code: givenName + ' ' + familyName
                  target:
                    path: $projection/attributes/cn
                """);

        prismContext = StudioPrismContextService.getPrismContext(myFixture.getProject());
        parsingCtx = prismContext.createParsingContextForCompatibilityMode();
    }

    public void testPsiFile() {
        assertNotNull(psiFile);
        YAMLFile yamlFile = (YAMLFile) psiFile;
        YAMLDocument doc = yamlFile.getDocuments().get(0);
        YAMLMapping root = (YAMLMapping) doc.getTopLevelValue();
        assertNotNull(root);
    }

    public void testRootXNode() throws SchemaException {
        String rawElement = psiConverterImpl.convert(psiFile.getFirstChild().getFirstChild(), false);
        MapXNode mapping = prismContext.parserFor(rawElement)
                .language("yaml")
                .context(parsingCtx)
                .parseToXNode().toMapXNode();

        assertNotNull(mapping);
        assertEquals(MappingType.class, mapping.getDefinition().getTypeClass());
    }
}
