package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesCompletionContributor extends CompletionContributorBase {

    public SPropertiesCompletionContributor() {
        extend(CompletionType.BASIC,
                psiElement().inside(
                        PlatformPatterns.psiElement(SPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.IDENTIFIER))),
                new SPropertiesKeyCompletionProvider());

        // todo this one and previous one should not overlap - previous one should not be used inside IDENTIFIER that's inside PATH element,
        // this one should be used only inside IDENTIFIER that's inside PATH element
        extend(CompletionType.BASIC,
                psiElement().inside(
                        PlatformPatterns.psiElement(SPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.IDENTIFIER))),
                new SPropertiesKeyCompletionProvider());

        // todo provide list of properties (also encrypted) keys
    }
}
