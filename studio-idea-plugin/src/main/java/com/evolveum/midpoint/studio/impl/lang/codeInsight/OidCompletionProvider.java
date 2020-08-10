package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
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

        result.addElement(MidPointUtils.buildLookupElement("Random OID", UUID.randomUUID().toString(), "", 100));


        // todo this only when we're not adding oid to "ObjectType"
        List<OidNameValue> oids = ObjectFileBasedIndexImpl.getAllOidNames(parameters.getEditor().getProject());
        Collections.sort(oids, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));

        if (!oids.isEmpty()) {
            oids.forEach(o -> result.addElement(MidPointUtils.buildLookupElement(o.getName(), o.getOid(), "", 110)));
        }

//        result.addElement(MPUtils.buildLookupElement("oid", "oid", "OidCompletionProvider", 90));

//        ((XmlTag)((XmlTag)((XmlTag)parameters.getPosition().getParent().
//                getParent().getParent()).getDescriptor().getDeclaration()))
//                .getAttribute("type").getValueElement().getReferences() -> returns more, one of them schemaxsd something

//        ((SchemaPrefixReference)((XmlTag)((XmlTag)((XmlTag)parameters.getPosition().getParent().
//                getParent().getParent()).getDescriptor().getDeclaration()))
//                .getAttribute("type").getValueElement().getReferences()[1])
    }
}
