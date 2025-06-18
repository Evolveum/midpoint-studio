package com.evolveum.midpoint.studio.impl.lang.validator;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.converter.JsonToXNode;
import com.evolveum.midpoint.studio.impl.lang.converter.XNodeConverter;
import com.evolveum.midpoint.studio.impl.lang.converter.XmlToXNode;
import com.evolveum.midpoint.studio.impl.lang.converter.YamlToXNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.json.JsonLanguage;
import org.jetbrains.yaml.YAMLLanguage;

import java.io.IOException;
import java.util.List;

/**
 * Created by Dominik.
 */
public class XNodeExternalAnnotator extends ExternalAnnotator<PsiFile, List<String>> {

    PrismContext prismContext;
    XNodeConverter xNodeConverterImpl;

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file) {
        prismContext = StudioPrismContextService.getPrismContext(file.getProject());
        Language lang = file.getLanguage();

        if (lang.equals(XMLLanguage.INSTANCE)) {
            xNodeConverterImpl = new XmlToXNode();
        } else if (lang.equals(JsonLanguage.INSTANCE)) {
            xNodeConverterImpl = new JsonToXNode();
        } else if (lang.equals(YAMLLanguage.INSTANCE)) {
            xNodeConverterImpl = new YamlToXNode();
        }

        return file;
    }

    @Override
    public @Nullable List<String> doAnnotate(PsiFile psiFile) {

//        XNode xNode = xNodeBuilderImpl.buildFromPsi(psiFile);

        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                XNode xNode = xNodeConverterImpl.convertFromPsi(element);

                if (xNode != null) {
                    try {
                        validate(xNode);
                    } catch (SchemaException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                super.visitElement(element);
            }
        });

        return super.doAnnotate(psiFile);
    }

    private void validate(XNode xNode) throws SchemaException, IOException {
        var parsingCtx = prismContext.createParsingContextForCompatibilityMode();
        prismContext.parserFor(xNode.toRootXNode()).context(parsingCtx);

        parsingCtx.getWarnings().forEach(error -> {
            System.out.println("ERROR: " + error);
        });
    }
}
