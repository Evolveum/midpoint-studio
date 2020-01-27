package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorUtils.class);

    private GeneratorContext context;

    public ProcessorUtils(GeneratorContext context) {
        this.context = context;
    }

    public List<ObjectTemplateType> loadTemplates() throws Exception {
        return loadObjects(ObjectTemplateType.class);
    }

    public List<ResourceType> loadResources() throws Exception {
        List<ResourceType> resources = loadObjects(ResourceType.class);

        for (ResourceType resource : resources) {
            if (resource.getSchemaHandling() == null) {
                continue;
            }

            SchemaHandlingType schemaHandling = resource.getSchemaHandling();
            // todo sort
//            Collections.sort(schemaHandling.getObjectType(), (o1, o2) -> {
//
//                o1.getDisplayName();
//                o1.getKind();
//                o1.getIntent();
//
//                o1.getDisplayName();
//                o1.getKind();
//                o1.getIntent();
//            });
        }

        return resources;
    }

    public List<FunctionLibraryType> loadFunctionLibraries() throws Exception {
        return loadObjects(FunctionLibraryType.class);
    }

    private <T extends ObjectType> List<T> loadObjects(Class<T> type) throws Exception {
        List<T> objects = new ArrayList<>();

        MidPointClient client = context.getClient();
        List<T> result = client.list(type);
        if (result != null) {
            objects.addAll(result);
            Collections.sort(objects, new ObjectTypeComparator());
        }

        return objects;
    }

    private static class ObjectTypeComparator implements Comparator<ObjectType> {

        @Override
        public int compare(ObjectType o1, ObjectType o2) {
            String s1 = getName(o1);
            String s2 = getName(o2);

            return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
        }

        private String getName(ObjectType o) {
            if (o == null) {
                return null;
            }

            return TemplateUtils.getOrig(o.getName());
        }
    }
}
