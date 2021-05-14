package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentHolderType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ItemPathCacheService {

    private static final Set<ItemName> IGNORED;

    static {
        Set<ItemName> set = new HashSet<>();
        set.addAll(Arrays.asList(
                AssignmentHolderType.F_LENS_CONTEXT
        ));

        IGNORED = Collections.unmodifiableSet(set);
    }

    private Project project;

    private Map<QName, Set<String>> paths = new HashMap<>();

    public ItemPathCacheService(Project project) {
        this.project = project;

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void environmentChanged(Environment oldEnv, Environment newEnv) {
                refresh(newEnv);
            }
        });

        EnvironmentService env = project.getService(EnvironmentService.class);
        refresh(env.getSelected());
    }

    public void refresh(Environment env) {
        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
            MidPointClient client = new MidPointClient(project, env);
            PrismContext context = client.getPrismContext();
            SchemaRegistry registry = context.getSchemaRegistry();

            Map<QName, Set<String>> map = new HashMap<>();
            for (PrismSchema schema : registry.getSchemas()) {
                for (PrismObjectDefinition def : schema.getObjectDefinitions()) {
                    Set set = new HashSet();

                    buildPaths(new IdentityHashMap<>(), def, "", set);

                    map.put(def.getTypeName(), Collections.unmodifiableSet(set));
                }
            }

            paths = map;
        });
    }

    private void buildPaths(IdentityHashMap<Definition, Boolean> visited, Definition definition, String parentPath, Set<String> allPaths) {
        if (visited.containsKey(definition)) {
            return;
        }

        visited.put(definition, true);

        if (!(definition instanceof ItemDefinition)) {
            return;
        }

        ItemDefinition id = (ItemDefinition) definition;
        String localPart = id.getItemName().getLocalPart();

        if (IGNORED.contains(id.getItemName())) {
            return;
        }

        if (definition instanceof PrismPropertyDefinition || definition instanceof PrismReferenceDefinition) {
            String newPath = StringUtils.isEmpty(parentPath) ? localPart : parentPath + "/" + localPart;
            allPaths.add(newPath);

            return;
        }

        if (!(id instanceof PrismContainerDefinition)) {
            return;
        }

        PrismContainerDefinition<? extends ItemDefinition> pcd = (PrismContainerDefinition) id;

        if (pcd instanceof PrismObjectDefinition) {
            if (pcd.isAbstract()) {
                return;
            }

            if (!FocusType.class.isAssignableFrom(pcd.getCompileTimeClass())) {
                return;
            }

            for (ItemDefinition def : pcd.getDefinitions()) {
                buildPaths(visited, def, "", allPaths);
            }
        }

        if (pcd instanceof PrismContainerDefinition) {
            String path = StringUtils.isEmpty(parentPath) ? localPart : parentPath + "/" + localPart;
            allPaths.add(path);

            if (pcd.isMultiValue()) {
                return;
            }

            String newPath = StringUtils.isEmpty(parentPath) ? localPart : parentPath + "/" + localPart;

            for (ItemDefinition def : pcd.getDefinitions()) {
                buildPaths(visited, def, newPath, allPaths);
            }
        }
    }

    public Set<String> getAvailablePaths(QName type) {
        Set<String> set = new HashSet<>();

        if (type == null) {
            paths.forEach((q, s) -> set.addAll(s));
        } else {
            Set<String> s = paths.get(type);
            if (s != null) {
                set.addAll(s);
            }
        }

        return Collections.unmodifiableSet(set);
    }
}
