package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.Definition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ObjectsBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    public static final Map<Class<? extends ObjectType>, List<ItemPath>> CLEANUP_PATHS;

    static {
        Map<Class<? extends ObjectType>, List<ItemPath>> map = createCleanupPaths();
        CLEANUP_PATHS = Collections.unmodifiableMap(map);
    }

    public CleanupFileTask(@NotNull AnActionEvent event) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
    }

    private static Map<Class<? extends ObjectType>, List<ItemPath>> createCleanupPaths() {
        Map<Class<? extends ObjectType>, List<ItemPath>> result = new HashMap<>();

        Arrays.stream(ObjectTypes.values())
                .map(ot -> ot.getClassDefinition())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    Definition definition = MidPointUtils.DEFAULT_PRISM_CONTEXT.getSchemaRegistry()
                            .findObjectDefinitionByCompileTimeClass(clazz);

                    List<ItemPath> paths = createCleanupPaths(
                            definition, ItemPath.EMPTY_PATH, new ArrayList<>(), new IdentityHashMap<>());
                    sort(paths, comparing(ItemPath::toString));

                    result.put(clazz, paths);
                });

        return result;
    }

    private static List<ItemPath> createCleanupPaths(
            Definition definition, ItemPath parentPath, List<ItemPath> paths, IdentityHashMap<Definition, Boolean> visited) {

        if (visited.containsKey(definition)) {
            return paths;
        }

        visited.put(definition, true);

        if (!(definition instanceof ItemDefinition<?> itemDef)) {
            return paths;
        }

        if (itemDef.isOperational()) {
            paths.add(parentPath.append(itemDef.getItemName()));
            return paths;
        }

        if (itemDef instanceof PrismContainerDefinition<?> containerDef) {

            ItemPath newParentPath = itemDef instanceof PrismObjectDefinition ?
                    parentPath : parentPath.append(itemDef.getItemName());

            for (ItemDefinition<?> def : containerDef.getDefinitions()) {
                createCleanupPaths(def, newParentPath, paths, visited);
            }
        }

        return paths;
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject object) throws Exception {
        String newContent = cleanupObject(object);

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
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    private String cleanupObject(MidPointObject object) {
        String content = object.getContent();
        if (content == null) {
            return null;
        }

        Document doc = DOMUtil.parseDocument(content);
        Element root = doc.getDocumentElement();

        ObjectTypes type = object.getType();
        if (type == null) {
            return content;
        }

        boolean cleaned = false;
        List<ItemPath> paths = CLEANUP_PATHS.get(type.getClassDefinition());
        for (ItemPath path : paths) {
            cleaned = cleaned | cleanupObject(root, path);
        }

        if (!cleaned) {
            return content;
        }

        DOMUtil.normalize(root, false);
        return DOMUtil.serializeDOMToString(doc);
    }

    private static boolean cleanupObject(Element obj, ItemPath path) {
        if (path.isEmpty()) {
            obj.getParentNode().removeChild(obj);
            return true;
        }

        boolean cleaned = false;
        List<Element> elements = DOMUtil.getChildElements(obj, path.firstToName());
        for (Element e : elements) {
            cleaned = cleaned | cleanupObject(e, path.rest());
        }

        return cleaned;
    }
}
