package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.InitialObjectsCache;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
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
            result.addElement(MidPointUtils.buildOidLookupElement("Random OID", UUID.randomUUID().toString(), null, "", 110));
            return;
        }

        Project project = parameters.getEditor().getProject();

        addLocalFiles(project, result);
        addInitialObjects(project, result);
    }

    private void addInitialObjects(Project project, CompletionResultSet result) {
        InitialObjectsCache cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_INITIAL_OBJECTS);
        if (cache == null) {
            return;
        }

        cache.list().forEach(
                o -> {
                    String name = MidPointUtils.getDisplayNameOrName(o);
                    if (name == null) {
                        name = "Undefined";
                    }

                    ObjectTypes type = ObjectTypes.getObjectType(o.getClass());

                    result.addElement(
                            MidPointUtils.buildOidLookupElement(
                                    name,
                                    o.getOid(),
                                    type.getTypeQName(),
                                    StudioLocalization.get().translateEnum(type),
                                    90));
                });
    }

    private void addLocalFiles(Project project, CompletionResultSet result) {
        List<OidNameValue> oids = ObjectFileBasedIndexImpl.getAllOidNames(project);
        if (oids.isEmpty()) {
            return;
        }

        Collections.sort(oids, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));

        oids.forEach(
                o -> result.addElement(
                        MidPointUtils.buildOidLookupElement(
                                o.getName(),
                                o.getOid(),
                                o.getType().getTypeQName(),
                                o.getSource(),
                                100)));
    }
}
