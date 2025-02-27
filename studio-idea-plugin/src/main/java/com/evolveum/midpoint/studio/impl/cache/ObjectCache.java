package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ObjectCache<O extends ObjectType> extends Cache {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectCache.class);

    private final Class<O> type;

    private final Map<String, O> cache = new HashMap<>();

    private long lastReloadTime;

    public ObjectCache(@NotNull Project project, @NotNull Class<O> type) {
        super(project);

        this.type = type;
    }

    public synchronized O get(String oid) {
        if (System.currentTimeMillis() - lastReloadTime > getTtl() * 1000) {
            reload();
        }

        return cache.get(oid);
    }

    public synchronized Collection<O> list() {
        if (System.currentTimeMillis() - lastReloadTime > getTtl() * 1000) {
            reload();
        }

        return cache.values();
    }

    @Override
    void clear() {
        LOG.debug("Clearing cache for {}: ", getClass().getSimpleName(), type.getSimpleName());

        cache.clear();

        LOG.debug("Cache cleared for {}: {}", getClass().getSimpleName(), type.getSimpleName());
    }

    public synchronized void reload() {
        LOG.debug("Reloading cache for {}: {}", getClass().getSimpleName(), type.getSimpleName());
        long time = System.currentTimeMillis();

        MidPointClient client = new MidPointClient(null, getEnvironment(), true, true);

        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        if (LookupTableType.class.equals(type)) {
            options.add(SelectorOptions.create(UniformItemPath.create(LookupTableType.F_ROW),
                    GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        }

        SearchResult result = client.search(type, createReloadQuery(), options);
        cacheObjects(result.getObjects());

        lastReloadTime = System.currentTimeMillis();

        LOG.debug("Cache reloaded for {}: {}, size: {} in {}ms",
                getClass().getSimpleName(), type.getSimpleName(), cache.size(), (System.currentTimeMillis() - time));
    }

    protected ObjectQuery createReloadQuery() {
        return null;
    }

    protected void cacheObjects(Collection<MidPointObject> objects) {
        cache.clear();

        for (MidPointObject object : objects) {
            cacheObject(object);
        }

        lastReloadTime = System.currentTimeMillis();
    }

    protected void cacheObject(MidPointObject object) {
        try {
            PrismParser parser = ClientUtils.createParser(PrismContext.get(), object.getContent());

            PrismObject<?> prismObject = parser.parse();
            O obj = (O) prismObject.asObjectable();

            cache.put(obj.getOid(), obj);
        } catch (Exception ex) {
            if (ex instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) ex;
            }
            LOG.debug("Couldn't parse object", ex);
        }
    }
}
