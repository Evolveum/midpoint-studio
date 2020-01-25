package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FunctionLibraryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectTemplateType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
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
        return loadObjects(ResourceType.class);
    }

    public List<FunctionLibraryType> loadFunctionLibraries() throws Exception {
        return loadObjects(FunctionLibraryType.class);
    }

    private <T extends ObjectType> List<T> loadObjects(Class<T> type) throws Exception {
//        Map<T, Set<String>> types = context.getConfiguration().includedObjectTypes();
//        Set<String> oids = types.get(type);

        // todo filter by oids

        List<T> objects = new ArrayList<>();

        Service service = context.getClient();
        try {
            SearchResult result = service.collection(type).search().queryFor(type).build().get();
            if (result != null) {
                objects.addAll(result);
                Collections.sort(objects, new ObjectTypeComparator());
            }
        } catch (ObjectNotFoundException ex) {
            LOG.warn("Couldn't find objects of type {}", type);
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
