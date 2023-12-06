package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.util.cleanup.CleanupActionProcessor;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ObjectsBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    public static final Map<Class<? extends ObjectType>, Set<ItemPath>> CLEANUP_PATHS;

    static {
        Map<Class<? extends ObjectType>, Set<ItemPath>> map = createCleanupPaths();
        CLEANUP_PATHS = Collections.unmodifiableMap(map);
    }

    public CleanupFileTask(@NotNull AnActionEvent event) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
    }

    private static Map<Class<? extends ObjectType>, Set<ItemPath>> createCleanupPaths() {
        Map<Class<? extends ObjectType>, Set<ItemPath>> result = new HashMap<>();

        Arrays.stream(ObjectTypes.values())
                .map(ObjectTypes::getClassDefinition)
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    Definition definition = MidPointUtils.DEFAULT_PRISM_CONTEXT.getSchemaRegistry()
                            .findObjectDefinitionByCompileTimeClass(clazz);

                    Set<ItemPath> paths = createCleanupPaths(
                            definition, ItemPath.EMPTY_PATH, new HashSet<>(), new IdentityHashMap<>());

                    result.put(clazz, paths);
                });

        return result;
    }

    private static Set<ItemPath> createCleanupPaths(
            Definition definition, ItemPath parentPath, Set<ItemPath> paths, IdentityHashMap<Definition, Set<ItemPath>> visited) {

        if (visited.containsKey(definition)) {
            for (ItemPath path : visited.get(definition)) {
                paths.add(parentPath.append(path));
            }

            return paths;
        }

        Set<ItemPath> definitionPaths = new HashSet<>();
        visited.put(definition, definitionPaths);

        if (!(definition instanceof ItemDefinition<?> itemDef)) {
            return paths;
        }

        if (itemDef.isOperational()) {
            definitionPaths.add(parentPath.append(itemDef.getItemName()));

            paths.addAll(definitionPaths);
            return paths;
        }

        if (itemDef instanceof PrismContainerDefinition<?> containerDef) {
            ItemPath newParentPath = parentPath;

            if (!(itemDef instanceof PrismObjectDefinition)) {
                newParentPath = parentPath.append(itemDef.getItemName());
            }

            for (ItemDefinition<?> def : containerDef.getDefinitions()) {
                createCleanupPaths(def, newParentPath, definitionPaths, visited);
            }
        }

        paths.addAll(definitionPaths);
        return paths;
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject object) {
        String oldContent = object.getContent();
        String newContent = cleanupObject(object);

        if (Objects.equals(oldContent, newContent)) {
            return new ProcessObjectResult(null)
                    .object(object);
        }

        MidPointObject newObject = MidPointObject.copy(object);
        newObject.setContent(newContent);

        return new ProcessObjectResult(null)
                .object(newObject);
    }

    @Override
    protected boolean isUpdateObjectAfterProcessing() {
        return true;
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private String cleanupObject(MidPointObject object) {
        String content = object.getContent();
        if (content == null) {
            return null;
        }

        try {
            PrismParser parser = ClientUtils.createParser(MidPointUtils.DEFAULT_PRISM_CONTEXT, content);
            List<PrismObject<? extends ObjectType>> objects = (List) parser.parseObjects();
            if (objects.isEmpty()) {
                return content;
            }

            List<PrismObject<? extends ObjectType>> result = (List) objects.stream().map(o -> o.clone()).toList();

            CleanupActionProcessor processor = new CleanupActionProcessor();
            for (PrismObject<? extends ObjectType> obj : result) {
                processor.process(obj);
            }

            if (objects.equals(result)) {
                return content;
            }

            PrismSerializer<String> serializer = ClientUtils.getSerializer(MidPointUtils.DEFAULT_PRISM_CONTEXT);
            if (result.size() == 1) {
                return serializer.serialize(result.get(0));
            }

            return serializer.serializeObjects((List) result);
        } catch (SchemaException | IOException ex) {
            // todo handle properly
            ex.printStackTrace();
        }

        return content;
    }
}
