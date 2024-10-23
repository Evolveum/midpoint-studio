package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlTagNameProvider;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExpressionEvaluatorTagNameProvider implements XmlTagNameProvider {

    private static final List<LookupElement> EVALUATORS;

    static {
        QName[] evaluators = {
                SchemaConstantsGenerated.C_VALUE,
                SchemaConstantsGenerated.C_AS_IS,
                SchemaConstantsGenerated.C_CONST,
                SchemaConstantsGenerated.C_FUNCTION,
                SchemaConstantsGenerated.C_PROPORTIONAL,
                SchemaConstantsGenerated.C_GENERATE,
                SchemaConstantsGenerated.C_SCRIPT,
                SchemaConstantsGenerated.C_PATH,
                SchemaConstantsGenerated.C_ASSOCIATION_TARGET_SEARCH,
                SchemaConstantsGenerated.C_ASSIGNMENT_TARGET_SEARCH,
                SchemaConstantsGenerated.C_REFERENCE_SEARCH,
                SchemaConstantsGenerated.C_ASSIGNMENT_FROM_ASSOCIATION,
                SchemaConstantsGenerated.C_ASSOCIATION_FROM_LINK,
                SchemaConstantsGenerated.C_SEQUENTIAL_VALUE

        };
        List<QName> names = new ArrayList<>();
        names.addAll(Arrays.asList(evaluators));

        Collections.sort(names, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getLocalPart(), o2.getLocalPart()));

        List<LookupElement> elements = names.stream().map(q ->
                LookupElementBuilder.create(q.getLocalPart())
                        .withIcon(AllIcons.Nodes.Tag)
                        .withTypeText(q.getNamespaceURI())
                        .withLookupStrings(Arrays.asList(q.getLocalPart(), q.getNamespaceURI()))
                        .withCaseSensitivity(true)
        ).collect(Collectors.toList());

        EVALUATORS = Collections.unmodifiableList(elements);
    }

    @Override
    public void addTagNameVariants(List<LookupElement> elements, @NotNull XmlTag tag, String prefix) {
        if (!MidPointUtils.hasMidPointFacet(tag.getProject())) {
            return;
        }

        if (!PlatformPatterns.psiElement()
                .withParent(MidPointUtils.qualifiedTag(SchemaConstantsGenerated.C_EXPRESSION))
                .accepts(tag)) {
            return;
        }

        elements.addAll(EVALUATORS);
    }
}
