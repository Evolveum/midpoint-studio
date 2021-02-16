package com.evolveum.midscribe.generator;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midscribe.util.Expander;
import com.evolveum.midscribe.util.InMemoryFileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryClient implements MidPointClient {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryClient.class);

    private Map<Class<? extends ObjectType>, List<ObjectType>> objects = new HashMap<>();

    private GenerateOptions options;

    private PrismContext prismContext;

    public InMemoryClient(GenerateOptions options) {
        Validate.notNull(options);

        this.options = options;
    }

    @Override
    public void init() throws Exception {
        Properties expanderProperties = new Properties();
        if (options.isExpand()) {
            File file = options.getExpanderProperties();
            if (file == null || !file.isFile() || !file.canRead()) {
                LOG.error("Expander properties file doesn't exist or can't be read '{}'", file);
            } else {
                try (InputStream is = new FileInputStream(options.getExpanderProperties())) {
                    expanderProperties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    LOG.error("Couldn't load midscribe.properties from classpath", ex);
                }
            }
        }

        Expander expander = new Expander(expanderProperties);

        LOG.debug("Initializing prism context");

        DOMUtilSettings.setAddTransformerFactorySystemProperty(false);

        PrismContextFactory factory = new MidPointPrismContextFactory();
         prismContext = factory.createPrismContext();
        prismContext.initialize();

        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();

        Iterator<File> files = FileUtils.iterateFiles(options.getSourceDirectory(),
                new InMemoryFileFilter(options.getInclude(), options.getExclude()), TrueFileFilter.INSTANCE);
        while (files.hasNext()) {
            File file = files.next();

            LOG.debug("Loading {}", file);

            try (InputStream is = new FileInputStream(file)) {
                List<PrismObject<? extends Objectable>> objects;
                if (options.isExpand()) {
                    InputStream expanded = expander.expand(is, StandardCharsets.UTF_8);
                    PrismParser parser = prismContext.parserFor(expanded).language(PrismContext.LANG_XML).context(parsingContext);
                    objects = parser.parseObjects();
                } else {
                    PrismParser parser = prismContext.parserFor(is).language(PrismContext.LANG_XML).context(parsingContext);
                    objects = parser.parseObjects();
                }

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
    public void destroy() throws Exception {
        objects.clear();
        objects = null;
    }

    @Override
    public PrismContext getPrismContext() {
        return prismContext;
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
