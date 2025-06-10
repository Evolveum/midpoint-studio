package com.evolveum.midpoint.studio.lang.groovy;

import com.evolveum.midpoint.studio.impl.cache.OpenApiTypeMappingCacheService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

import java.util.List;

/**
 * Created by Dominik.
 */
public class SchemaContributor extends CompletionContributor {
    public SchemaContributor() {



        // Pattern to detect Groovy DSL literal after "jsonType" key in closure
        PsiElementPattern.Capture<GrLiteral> pattern = PlatformPatterns.psiElement(GrLiteral.class)
                .withParent( // the argument list, or expression list in Groovy
                    PlatformPatterns.psiElement()
                            .withParent(
                                PlatformPatterns.psiElement()
                                    .withName("openApiType")
                            )
        );

        extend(CompletionType.BASIC,
                pattern,
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        Project project = parameters.getEditor().getProject();
                        if (project == null) return;

                        OpenApiTypeMappingCacheService cacheService = project.getService(OpenApiTypeMappingCacheService.class);
                        List<OpenApiTypeConstant> openApiTypeConstants = cacheService.get(OpenApiTypeConstant.OPEN_API_TYPE_MAPPING_ENUM);

                        if (openApiTypeConstants != null) {
                            openApiTypeConstants.forEach(openApiTypeConstant -> {
                                result.addElement(LookupElementBuilder.create(openApiTypeConstant.primaryWireType())
                                        .withTailText(" - " + openApiTypeConstant.openApiFormat(), true)
                                        .withTailText("openApiType", true));
                            });
                        }
                    }
                });
    }
}
