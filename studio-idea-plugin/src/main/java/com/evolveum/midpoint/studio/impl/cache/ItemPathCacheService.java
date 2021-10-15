package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ItemPathCacheService {

    private static final Logger LOG = Logger.getInstance(ItemPathCacheService.class);

    private static final Set<ItemName> IGNORED;

    static {
        Set<ItemName> set = new HashSet<>(Arrays.asList(
                AssignmentHolderType.F_LENS_CONTEXT,
                UserType.F_ADMIN_GUI_CONFIGURATION,
                ArchetypeType.F_ARCHETYPE_POLICY
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
        LOG.info("Invoking refresh");

        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
            LOG.info("Refreshing started");

            MidPointClient client = new MidPointClient(project, env);
            PrismContext context = client.getPrismContext();
            SchemaRegistry registry = context.getSchemaRegistry();

            Map<QName, Set<String>> map = new HashMap<>();
            for (PrismSchema schema : registry.getSchemas()) {
                for (PrismObjectDefinition def : schema.getObjectDefinitions()) {
                    Set<String> set = new HashSet<>();

                    buildPaths(new IdentityHashMap<>(), def, "", set);

                    map.put(def.getTypeName(), Collections.unmodifiableSet(set));
                }
            }

            paths = map;

            LOG.info("Refresh finished");
        });

        LOG.info("Refresh invoked");
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
//            if (pcd.isAbstract()) {
//                return;
//            }

            if (!FocusType.class.isAssignableFrom(pcd.getCompileTimeClass()) && !ShadowType.class.isAssignableFrom(pcd.getCompileTimeClass())) {
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

    public Map<String, List<ObjectTypes>> getAvailablePaths(QName type) {
        Map<String, List<ObjectTypes>> pathWithTypes = new HashMap<>();

        List<ObjectTypes> typeHierarchy;
        if (type != null) {
            ObjectTypes ot = ObjectTypes.getObjectTypeFromTypeQName(type);

            typeHierarchy = Arrays.stream(ObjectTypes.values())
                    .filter(o -> ot.getClassDefinition().isAssignableFrom(o.getClassDefinition()))
                    .collect(Collectors.toList());
        } else {
            typeHierarchy = Arrays.asList(ObjectTypes.values());
        }

        for (ObjectTypes o : typeHierarchy) {
            Set<String> paths = this.paths.get(o.getTypeQName());
            if (paths == null) {
                continue;
            }

            for (String path : paths) {
                List<ObjectTypes> pathTypes = pathWithTypes.computeIfAbsent(path, k -> new ArrayList<>());

                checkSubtypesAndAdd(pathTypes, o);
            }
        }

        pathWithTypes.values().forEach(Collections::sort);

        return pathWithTypes;
    }

    private void checkSubtypesAndAdd(List<ObjectTypes> pathTypes, ObjectTypes o) {
        Set<Class<? extends ObjectType>> classes = pathTypes.stream().map(ObjectTypes::getClassDefinition).collect(Collectors.toSet());

        Class<? extends ObjectType> clazz = o.getClassDefinition();

        if (classes.isEmpty()) {
            pathTypes.add(o);
            return;
        }

        if (classes.contains(clazz)) {
            return;
        }

        boolean addNew = true;
        Set<ObjectTypes> toDelete = new HashSet<>();
        for (ObjectTypes ot : pathTypes) {
            if (clazz.isAssignableFrom(ot.getClassDefinition())) {
                toDelete.add(ot);
            }
            if (ot.getClassDefinition().isAssignableFrom(clazz)) {
                addNew = false;
            }
        }
        pathTypes.removeAll(toDelete);

        if (addNew) {
            pathTypes.add(o);
        }
    }
}
