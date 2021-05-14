package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.model.api.ModelPublicConstants;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskHandlerAnnotatorCompletionProvider extends CompletionProvider<CompletionParameters> implements Annotator {

    public static final List<LookupElement> HANDLERS;

    static {
        List<String> handlers = new ArrayList<>();

        Field[] fields = ModelPublicConstants.class.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                // not public static final
                continue;
            }

            if (String.class != field.getType()) {
                continue;
            }

            try {
                String value = (String) field.get(ModelPublicConstants.class);
                if (value == null || !value.startsWith(SchemaConstants.NS_MODEL)) {
                    continue;
                }

                handlers.add(value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
            }
        }

        Collections.sort(handlers);

        List<LookupElement> list = new ArrayList<>();
        for (String s : handlers) {
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
            holder.createErrorAnnotation(element, "Handler URI not defined");
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
            holder.createWarningAnnotation(element, "Unknown handler uri");
        }
    }
}
