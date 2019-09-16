package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefOidCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {


//        Service client = manager.getClient();
//        List<ObjectTemplateType> templates = client.searchObjects(ObjectTemplateType.class, null, null);
//
//        for (ObjectTemplateType template : templates) {
//            result.addElement(
//                    MPUtils.buildLookupElement(MPUtils.getOrig(template.getName()), template.getOid(), "Template", 90)
//            );
//        }
//
////        result.addElement(
////                MPUtils.buildLookupElement("My User Template", UUID.randomUUID().toString(), "from cache", 90));
//
//
        result.addElement(
                MidPointUtils.buildLookupElement("oid", "oid", "RefOidCompletionProvider", 80));

        // todo implement
    }

}
