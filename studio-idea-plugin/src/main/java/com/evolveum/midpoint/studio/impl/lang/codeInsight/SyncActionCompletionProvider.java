package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.SyncAction;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SyncActionCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final List<LookupElement> ACTIONS;

    static {
        List<String> actions = new ArrayList<>();
        Arrays.asList(SyncAction.values()).forEach(a -> actions.add(a.getUri()));

        Collections.sort(actions);

        List<LookupElement> list = new ArrayList<>();
        for (String s : actions) {
            QName name = QNameUtil.uriToQName(s);
            // todo this buildOidLookupElement is not very nice reuse of builder
            list.add(MidPointUtils.buildOidLookupElement(name.getLocalPart(), s, null,
                    SyncAction.class.getSimpleName(), 100));
        }

        ACTIONS = Collections.unmodifiableList(list);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        result.addAllElements(ACTIONS);
    }
}
