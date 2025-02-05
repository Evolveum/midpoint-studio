package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MetadataType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class InitialObjectsCache extends ObjectCache<ObjectType> {

    private static final Logger LOG = Logger.getInstance(InitialObjectsCache.class);

    public InitialObjectsCache(@NotNull Project project) {
        super(project, ObjectType.class);
    }

    public <O extends ObjectType> O get(Class<O> type, String oid) {
        ObjectType object = get(oid);
        return type.isAssignableFrom(object.getClass()) ? type.cast(object) : null;
    }

    public <O extends ObjectType> Collection<O> list(Class<O> type) {
        return list().stream()
                .filter(o -> type.isAssignableFrom(o.getClass()))
                .map(o -> type.cast(o))
                .toList();
    }

    @Override
    protected ObjectQuery createReloadQuery() {
        try {
            return StudioPrismContextService.runCallableWithProject(
                    getProject(),
                    () -> createInitialObjectsQuery(StudioPrismContextService.get(getProject())
                            .getPrismContext())
            );
        } catch (Exception ex) {
            LOG.error("Couldn't create initial objects cache reload query", ex);
        }

        return createInitialObjectsQuery(StudioPrismContextService.DEFAULT_PRISM_CONTEXT);
    }

    private ObjectQuery createInitialObjectsQuery(PrismContext ctx) {
        return ctx.queryFor(ObjectType.class)
                .item(ObjectType.F_METADATA, MetadataType.F_CREATE_CHANNEL)
                .eq(SchemaConstants.CHANNEL_INIT_QNAME)
                .build();
    }
}
