package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;
import com.intellij.patterns.XmlPatterns;

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
                                        XmlPatterns.xmlTag().withName("action")
                                                .withParent(
                                                        XmlPatterns.xmlTag().withName("authorization")))),
                new AuthorizationActionCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()
                                .withParent(XmlPatterns.xmlTag().withName("handlerUri"))
                                .withParent(XmlPatterns.xmlTag().withName("action"))),
                new SyncActionCompletionProvider());

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
}
