package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentPropertiesCache;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesKeyCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        Editor editor = parameters.getEditor();
        Project project = editor.getProject();

        if (project == null) {
            return;
        }

        EnvironmentPropertiesCache cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_PROPERTIES);
        Set<String> keys = cache.getKeys();

        List<String> sorted = new ArrayList<>(keys);
        Collections.sort(sorted);

        Document doc = editor.getDocument();
        VirtualFile vf = FileDocumentManager.getInstance().getFile(doc);

        for (String key : sorted) {
            result.addElement(build(key, cache.expandKeyForInlay(key, vf)));
        }
    }

    private LookupElement build(String key, String value) {
        if (value == null) {
            value = "";
        }

        LookupElementBuilder builder = LookupElementBuilder.create(key)
                .withTypeText(value)    // todo change to "property, encrypted, file, directory"
                .withLookupStrings(Arrays.asList(key, value, value.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 90);
    }
}
