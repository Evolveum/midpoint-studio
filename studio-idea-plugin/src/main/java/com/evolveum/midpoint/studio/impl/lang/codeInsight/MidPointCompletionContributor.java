package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.lang.properties.SPropertiesCompletionProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.prism.xml.ns._public.query_3.FilterClauseType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import org.jetbrains.annotations.NotNull;

import static com.evolveum.midpoint.studio.util.MidPointUtils.*;
import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointCompletionContributor extends DefaultCompletionContributor {

    public MidPointCompletionContributor() {
        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()),
                new SPropertiesCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlAttributeValue()
                                .withParent(
                                        XmlPatterns.xmlAttribute("oid"))),
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
                        XmlPatterns
                                .xmlText()
                                .withParent(
                                        commonTag("handlerUri").withParent(qualifiedTag(SchemaConstantsGenerated.C_TASK))
                                )),
                new TaskHandlerAnnotatorCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns.or(
                                XmlPatterns.xmlText().withParent(qualifiedTag(FilterClauseType.F_MATCHING)),
                                XmlPatterns.xmlText().withParent(commonTag("matchingRule")),
                                XmlPatterns.xmlText().withParent(annotationTag("matchingRule"))
                        )
                ),
                new MatchingRuleCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns.or(
                                XmlPatterns.xmlText().withParent(commonTag("path").withParent(commonTag("source"))),
                                XmlPatterns.xmlText().withParent(commonTag("path").withParent(commonTag("target")))
                        )
                ),
                new ItemPathCompletionProvider());

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns.xmlText().withParent(commonTag("panelType"))
                ),
                new PanelTypeCompletionProvider());
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getEditor() == null || parameters.getEditor().getProject() == null) {
            return;
        }

        Project project = parameters.getEditor().getProject();

        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        super.fillCompletionVariants(parameters, result);
    }
}
