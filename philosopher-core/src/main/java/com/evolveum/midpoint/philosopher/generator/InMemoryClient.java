package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryClient implements MidPointClient {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryClient.class);

    private Map<Class<? extends ObjectType>, List<ObjectType>> objects = new HashMap<>();

    private GenerateOptions options;

    public InMemoryClient(GenerateOptions options) {
        Validate.notNull(options);

        this.options = options;
    }

    @Override
    public void init() throws Exception {
        DOMUtilSettings.setAddTransformerFactorySystemProperty(false);
        // todo create web client just to obtain extension schemas!

        PrismContextFactory factory = new MidPointPrismContextFactory();
        PrismContext prismContext = factory.createPrismContext();
        prismContext.initialize();

        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();


        Iterator<File> files = FileUtils.iterateFiles(options.getSourceDirectory(), new String[]{"xml"}, true);
        while (files.hasNext()) {
            File file = files.next();

            try {
                PrismParser parser = prismContext.parserFor(file).language(PrismContext.LANG_XML).context(parsingContext);
                List<PrismObject<? extends Objectable>> objects = parser.parseObjects();

                for (PrismObject<? extends Objectable> object : objects) {
                    ObjectType obj = (ObjectType) object.asObjectable();

                    List<ObjectType> list = this.objects.get(obj.getClass());
                    if (list == null) {
                        list = new ArrayList<>();
                        this.objects.put(obj.getClass(), list);
                    }
                    list.add(obj);
                }
            } catch (Exception ex) {
                LOG.warn("Couldn't load file {}, reason: {}", file.getPath(), ex.getMessage());
            }
        }
        LOG.debug("Initialization done");
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
