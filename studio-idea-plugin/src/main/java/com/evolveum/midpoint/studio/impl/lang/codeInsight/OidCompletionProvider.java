package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {

        result.addElement(MidPointUtils.buildLookupElement("Random OID", UUID.randomUUID().toString(), "", 100));

//        result.addElement(MPUtils.buildLookupElement("oid", "oid", "OidCompletionProvider", 90));

//        ((XmlTag)((XmlTag)((XmlTag)parameters.getPosition().getParent().
//                getParent().getParent()).getDescriptor().getDeclaration()))
//                .getAttribute("type").getValueElement().getReferences() -> returns more, one of them schemaxsd something

//        ((SchemaPrefixReference)((XmlTag)((XmlTag)((XmlTag)parameters.getPosition().getParent().
//                getParent().getParent()).getDescriptor().getDeclaration()))
//                .getAttribute("type").getValueElement().getReferences()[1])
    }
}
