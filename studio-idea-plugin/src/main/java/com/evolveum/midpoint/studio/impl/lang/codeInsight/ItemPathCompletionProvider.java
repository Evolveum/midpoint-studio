package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.impl.cache.ItemPathCacheService;
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

import java.util.Arrays;
import java.util.Set;

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

        ItemPathCacheService cache = project.getService(ItemPathCacheService.class);
        // todo improve
        Set<String> paths = cache.getAvailablePaths(null);

        for (String path : paths) {
            result.addElement(build(path));
        }
    }

    private LookupElement build(String path) {
        LookupElementBuilder builder = LookupElementBuilder.create(path)
                .withTypeText("asdf")
                .withLookupStrings(Arrays.asList(path, path.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 80);
    }
}
