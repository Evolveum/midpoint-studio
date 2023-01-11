package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.search.JavaFilesSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.messages.MessageBus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PanelTypeCacheService {

    private Set<String> panels = new HashSet<>();

    private Project project;

    public PanelTypeCacheService(Project project) {
        this.project = project;

        // todo this should rather listen to some maven event and change in dependencies (mainly com.evolveum.midpoint.* group)
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void environmentChanged(Environment oldEnv, Environment newEnv) {
                refresh();
            }
        });

        refresh();
    }

    public void refresh() {
        if (project == null) {
            panels.clear();
            return;
        }

        SearchScope scope = new JavaFilesSearchScope(project);

        AllClassesSearch.SearchParameters params = new AllClassesSearch.SearchParameters(scope, project);
        PsiClass[] classes = AllClassesSearch.INSTANCE
                .createQuery(params)
                .filtering(c -> c.getAnnotation("com.evolveum.midpoint.web.application.PanelType") != null)
                .toArray(new PsiClass[0]);

        List<String> panelTypes = Arrays.stream(classes)
                .map(p -> p.getAnnotation("com.evolveum.midpoint.web.application.PanelType"))
                .map(a -> a.findAttributeValue("name").getText())
                .map(s -> s.replaceAll("\"", "").trim())
                .sorted()
                .collect(Collectors.toList());

        panels.clear();
        panels.addAll(panelTypes);
    }

    public Set<String> getPanels() {
        return Collections.unmodifiableSet(panels);
    }
}
