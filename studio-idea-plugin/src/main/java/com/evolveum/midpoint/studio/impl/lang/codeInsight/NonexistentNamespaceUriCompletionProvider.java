package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class NonexistentNamespaceUriCompletionProvider extends CompletionContributor {

    public static final String[] IGNORED_RESOURCES = {
            "http://midpoint.evolveum.com/xml/ns/public/common/org-3",
            "http://prism.evolveum.com/xml/ns/public/matching-rule-3",
            "http://midpoint.evolveum.com/xml/ns/public/resource/instance-3"
    };

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (!shouldAddVariants(parameters.getPosition())) {
            return;
        }

        addCompletions(parameters, result);
    }

    private boolean shouldAddVariants(PsiElement element) {
        return PlatformPatterns.psiElement().inside(
                XmlPatterns
                        .xmlAttributeValue()
                        .withParent(XmlPatterns.xmlAttribute().withName(StandardPatterns.string().startsWith("xmlns")))
        ).accepts(element);
    }

    private void addCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();

        XmlAttributeValue value = (XmlAttributeValue) element.getParent();

        XmlAttribute attr = (XmlAttribute) value.getParent();
        if (!attr.getName().startsWith("xmlns")) {
            return;
        }

        List<LookupElement> elements = new ArrayList<>();
        for (String url : IGNORED_RESOURCES) {
            elements.add(LookupElementBuilder.create(url));
        }

        result.addAllElements(elements);
    }
}
