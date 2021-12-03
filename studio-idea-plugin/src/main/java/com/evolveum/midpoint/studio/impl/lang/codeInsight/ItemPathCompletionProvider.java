package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.cache.ItemPathCacheService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ItemPathCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        // toto provide list of properties (also encrypted) keys
        if (parameters.getEditor() == null || parameters.getEditor().getProject() == null) {
            return;
        }

        Project project = parameters.getEditor().getProject();

        String text = parameters.getOriginalPosition().getText();
        QName type = null;
        if (text != null) {
            if (text.startsWith("$user")) {
                type = UserType.COMPLEX_TYPE;
            } else if (text.startsWith("$focus")) {
                type = FocusType.COMPLEX_TYPE;
            } else if (text.startsWith("$shadow") || text.startsWith("$projection")) {
                type = ShadowType.COMPLEX_TYPE;
            }
        }

        ItemPathCacheService cache = project.getService(ItemPathCacheService.class);

        Map<String, List<ObjectTypes>> pathsWithTypes = cache.getAvailablePaths(type);

        List<String> paths = new ArrayList<>();
        paths.addAll(pathsWithTypes.keySet());

        Collections.sort(paths);

        for (String path : paths) {
            result.addElement(build(text, type, path, pathsWithTypes.get(path)));
        }
    }

    private LookupElement build(String originalText, QName originalType, String path, List<ObjectTypes> types) {
        String prefix = "";
        if (originalType != null) {
            prefix = originalText;

            if (prefix.contains("/") && !prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.indexOf("/"));
            }

            if (!prefix.endsWith("/")) {
                prefix += "/";
            }
        }

        String typeText = types.stream().map(o -> o.getTypeQName().getLocalPart()).reduce((s1, s2) -> s1 + ", " + s2).get();

        LookupElementBuilder builder = LookupElementBuilder.create(prefix + path)
                .withTypeText(typeText)
                .withLookupStrings(Arrays.asList(path, path.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 80);
    }
}
