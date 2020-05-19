package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;
import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointCompletionContributor extends DefaultCompletionContributor {

    public MidPointCompletionContributor() {

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlAttributeValue()
                                .withParent(
                                        XmlPatterns.xmlAttribute("oid"))), // todo improve
                new OidCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()
                                .withParent(
                                        commonTag("action").withParent(commonTag("authorization"))
                                )),
                new AuthorizationActionCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()
                                .withParent(
                                        commonTag("handlerUri").withParent(commonTag("action"))
                                )),
                new SyncActionCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns.or(
                                XmlPatterns.xmlText().withParent(commonTag("matching")),
                                XmlPatterns.xmlText().withParent(commonTag("matchingRule")),
                                XmlPatterns.xmlText().withParent(annotationTag("matchingRule"))
                        )
                ),
                new MatchingRuleCompletionProvider());



//        extend(CompletionType.BASIC,
//                psiElement().inside(
//                        XmlPatterns
//                                .xmlAttributeValue()    //.withValue(string().equalTo(CompletionUtilCore.DUMMY_IDENTIFIER))
//                                .withParent(XmlPatterns.xmlAttribute("oid")
//                                        .withParent(XmlPatterns.xmlTag().withName("includeRef")))), // todo improve
//                new RefOidCompletionProvider());

//        extend(CompletionType.BASIC,
//                psiElement().inside(
//                        XmlPatterns
//                                .xmlAttributeValue().withValue(string().equalTo(CompletionUtilCore.DUMMY_IDENTIFIER))
//                                .withParent(XmlPatterns.xmlAttribute("type"))),
//                new RefTypeCompletionProvider());
    }

    private XmlTagPattern.Capture commonTag(String localName) {
        return qualifiedTag(localName, SchemaConstantsGenerated.NS_COMMON);
    }

    private XmlTagPattern.Capture annotationTag(String localName) {
        return qualifiedTag(localName, SchemaConstantsGenerated.NS_ANNOTATION);
    }

    private XmlTagPattern.Capture qualifiedTag(String localName, String namespace) {
        return XmlPatterns.xmlTag().withLocalName(localName).withNamespace(namespace);
    }
}
