package com.evolveum.midpoint.studio.lang.groovy;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyLanguage;

/**
 * Created by Dominik.
 */
public class SchemaContributor extends CompletionContributor {
    public SchemaContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PsiElement.class).withLanguage(GroovyLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        result.addElement(LookupElementBuilder.create("jsonType"));
                        result.addElement(LookupElementBuilder.create("defaultValue"));
                        result.addElement(LookupElementBuilder.create("required"));
                    }
                });
    }
}
