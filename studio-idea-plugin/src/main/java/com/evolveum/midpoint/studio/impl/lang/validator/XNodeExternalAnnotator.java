package com.evolveum.midpoint.studio.impl.lang.validator;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.converter.JsonToXNode;
import com.evolveum.midpoint.studio.impl.lang.converter.XNodeConverter;
import com.evolveum.midpoint.studio.impl.lang.converter.XmlToXNode;
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
            xNodeConverterImpl = new XmlToXNode();
        }

        return file;
    }

    @Override
    public @Nullable List<String> doAnnotate(PsiFile psiFile) {

//        XNode xNode = xNodeBuilderImpl.buildFromPsi(psiFile);

        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                XNode xNode = xNodeConverterImpl.convertFromPsi(element);

                if (xNode != null) {
                    try {
                        validate(xNode);
                    } catch (SchemaException | IOException e) {
                        System.out.println("EXCEPTION: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }

            }
        });

        return super.doAnnotate(psiFile);
    }

    private void walkPsiElement(@NotNull PsiElement element) {
        System.out.println("PSI ELEMENT: " + element.getText());

        for (PsiElement child : element.getChildren()) {
            walkPsiElement(child);
        }
    }

    private void printPsiElement(@NotNull PsiElement element, int indent) {
        String indentStr = "  ".repeat(indent);
        String text = element.getText().replace("\n", "\\n");
        if (text.length() > 60) text = text.substring(0, 60) + "...";

        System.out.println(indentStr + "- " + element.getClass().getSimpleName() + ": '" + text + "'");
        for (PsiElement child : element.getChildren()) {
            printPsiElement(child, indent + 1);
        }
    }

    private void validate(XNode xNode) throws SchemaException, IOException {

        prismContext.getSchemaRegistry().findItemDefinitionByElementName(xNode.getTypeQName());

        var parsingCtx = prismContext.createParsingContextForCompatibilityMode();
        var parser = prismContext.parserFor(xNode.toRootXNode()).context(parsingCtx);

        var object = parser.parseRealValue();

        parsingCtx.getWarnings().forEach(warning -> {
            System.out.println("ERROR: " + warning);
        });

    }
}
