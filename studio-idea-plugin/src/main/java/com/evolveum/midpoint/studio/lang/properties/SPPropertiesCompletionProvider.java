package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.impl.cache.PropertiesInlayCacheService;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPropertiesCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        // toto provide list of properties (also encrypted) keys
        if (parameters.getEditor() == null || parameters.getEditor().getProject() == null) {
            return;
        }

        Project project = parameters.getEditor().getProject();

        PropertiesInlayCacheService cache = project.getService(PropertiesInlayCacheService.class);
        Set<String> keys = cache.getKeys();

        List<String> sorted = new ArrayList<>();
        sorted.addAll(keys);

        Collections.sort(sorted);

        Document doc = parameters.getEditor().getDocument();
        VirtualFile vf = null;
        if (doc != null) {
            vf = FileDocumentManager.getInstance().getFile(doc);
        }

        for (String key : keys) {
            result.addElement(build(key, cache.expandKeyForInlay(key, vf)));
        }
    }

    private LookupElement build(String key, String value) {
        key = "$(" + key + ")";

        if (value == null) {
            value = "";
        }

        LookupElementBuilder builder = LookupElementBuilder.create(key)
                .withTypeText(value)
                .withLookupStrings(Arrays.asList(key, value, value.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 90);
    }
}
