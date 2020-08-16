package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final String[] NAMES;

    static {
        List<String> names = new ArrayList<>();
        for (ObjectTypes t : ObjectTypes.values()) {
            if (t.getClassDefinition() == null || Modifier.isAbstract(t.getClassDefinition().getModifiers())) {
                continue;
            }

            names.add(t.getElementName().getLocalPart());
        }

        NAMES = names.toArray(new String[names.size()]);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {

        PsiElement element = parameters.getPosition();

        if (isItObjectTypeOidAttribute(element)) {
            result.addElement(MidPointUtils.buildLookupElement("Random OID", UUID.randomUUID().toString(), "", 100));
        }

        List<OidNameValue> oids = ObjectFileBasedIndexImpl.getAllOidNames(parameters.getEditor().getProject());
        Collections.sort(oids, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));

        if (!oids.isEmpty()) {
            oids.forEach(o -> result.addElement(MidPointUtils.buildLookupElement(o.getName(), o.getOid(), "", 110)));
        }
    }

    private boolean isItObjectTypeOidAttribute(PsiElement element) {
        return psiElement().inside(
                XmlPatterns
                        .xmlAttributeValue()
                        .withParent(
                                XmlPatterns.xmlAttribute("oid").withParent(
                                        XmlPatterns.xmlTag().withNamespace(SchemaConstantsGenerated.NS_COMMON)
                                                .withName(NAMES)))).accepts(element);
    }
}
