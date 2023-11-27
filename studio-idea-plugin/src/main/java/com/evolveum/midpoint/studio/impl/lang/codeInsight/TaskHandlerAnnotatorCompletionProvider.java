package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.sdk.api.lang.TaskHandlerProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskHandlerAnnotatorCompletionProvider extends CompletionProvider<CompletionParameters> implements Annotator {

    public static final List<LookupElement> HANDLERS;

    static {
        TaskHandlerProvider provider = null;    // todo inject provider(s) here

        Set<String> handlers = provider != null ? provider.getTaskHandlerUris() : Collections.emptySet();
        List<String> sorted = new ArrayList<>(handlers);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);

        List<LookupElement> list = new ArrayList<>();
        for (String s : sorted) {
            String label = s.replace(SchemaConstants.NS_MODEL, "");

            LookupElementBuilder builder = LookupElementBuilder.create(s)
                    .withTypeText(label)
                    .withLookupStrings(Arrays.asList(s, s.toLowerCase()))
                    .withBoldness(true)
                    .withCaseSensitivity(true);

            LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

            list.add(PrioritizedLookupElement.withPriority(element, 100));
        }

        HANDLERS = Collections.unmodifiableList(list);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        result.addAllElements(HANDLERS);
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!MidPointUtils.hasMidPointFacet(element.getProject())) {
            return;
        }

        if (!(element instanceof XmlTag)) {
            return;
        }

        XmlTag tag = (XmlTag) element;
        if (!TaskType.F_HANDLER_URI.equals(MidPointUtils.createQName(tag))) {
            return;
        }

        XmlTag parent = tag.getParentTag();

        if (parent == null || !SchemaConstantsGenerated.C_TASK.equals(MidPointUtils.createQName(parent))) {
            return;
        }

        String uri = tag.getValue().getText();
        if (StringUtils.isEmpty(uri)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Handler URI not defined")
                    .range(element)
                    .create();
            return;
        }

        boolean found = false;
        for (LookupElement l : HANDLERS) {
            if (l.getLookupString().equals(uri)) {
                found = true;
                break;
            }
        }

        if (!found) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unknown handler uri")
                    .range(element)
                    .create();
        }
    }
}
