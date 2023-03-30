package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;

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
    }
}
