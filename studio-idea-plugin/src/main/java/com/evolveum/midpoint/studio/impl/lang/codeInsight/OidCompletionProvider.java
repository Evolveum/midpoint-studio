package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {

        PsiElement element = parameters.getPosition();

        if (MidPointUtils.isItObjectTypeOidAttribute(element)) {
            result.addElement(MidPointUtils.buildLookupElement("Random OID", UUID.randomUUID().toString(), "", 110));
            return;
        }

        List<OidNameValue> oids = ObjectFileBasedIndexImpl.getAllOidNames(parameters.getEditor().getProject());
        Collections.sort(oids, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));

        if (!oids.isEmpty()) {
            oids.forEach(o -> result.addElement(MidPointUtils.buildLookupElement(o.getName(), o.getOid(), o.getSource(), 100)));
        }
    }
}
