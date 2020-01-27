package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.philosopher.util.PhilosopherUtils;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LocalClient implements MidPointClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalClient.class);

    private Map<Class<? extends ObjectType>, List<ObjectType>> objects = new HashMap<>();

    private LocalOptions options;

    public LocalClient(LocalOptions options) {
        Validate.notNull(options);

        this.options = options;
    }

    @Override
    public void init() throws Exception {
        JAXBContext ctx = PhilosopherUtils.createJAXBContext();
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        Iterator<File> files = FileUtils.iterateFiles(options.getSourceDirectory(), new String[]{"xml"}, true);
        while (files.hasNext()) {
            File file = files.next();

            try {
                Object obj = unmarshaller.unmarshal(file);
                List<ObjectType> objects = getObjectType(obj);
                for (ObjectType object : objects) {
                    List<ObjectType> list = this.objects.get(object.getClass());
                    if (list == null) {
                        list = new ArrayList<>();
                        this.objects.put(object.getClass(), list);
                    }
                    list.add(object);
                }
            } catch (Exception ex) {
                LOG.warn("Couldn't load file {}, reason: {}", file.getPath(), ex.getMessage());
            }
        }
        LOG.debug("Initialization done");
    }

    private List<ObjectType> getObjectType(Object obj) {
        List<ObjectType> result = new ArrayList<>();

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof ObjectListType) {
            ObjectListType list = (ObjectListType) obj;
            result.addAll(list.getObject());
            return result;
        }

        if (obj instanceof ObjectType) {
            result.add((ObjectType) obj);
        }

        return result;
    }

    @Override
    public <T extends ObjectType> List<T> list(Class<T> type) {
        Validate.notNull(type);

        List<T> list = (List) objects.get(type);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    @Override
    public <T extends ObjectType> T get(Class<T> type, String oid) {
        Validate.notNull(type);
        Validate.notNull(oid);

        List<T> list = (List) objects.get(type);
        if (list == null) {
            return null;
        }

        for (T t : list) {
            if (oid.equals(t.getOid())) {
                return t;
            }
        }

        return null;
    }
}
