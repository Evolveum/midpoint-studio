package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.processor.ResourceSchemaRegistry;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

public class ResourceCache extends ObjectCache<ResourceType> {

    private final Function<String, PrismObject<ResourceType>> resourceLoader = (oid) -> get(oid).asPrismObject();
    private final ResourceSchemaRegistry resourceSchemaRegistry;

    public ResourceCache(@NotNull Project project) {
        super(project, ResourceType.class);
        this.resourceSchemaRegistry = PrismContext.get().getDefaultSchemaLookup().schemaSpecific(ResourceSchemaRegistry.class);
        if (resourceSchemaRegistry != null) {
            resourceSchemaRegistry
                    .registerResourceObjectLoader(resourceLoader);
        }
    }

    @Override
    void clear() {
        super.clear();
    }

    @Override
    protected void cacheObjects(Collection<MidPointObject> objects) {
        // FIXME: Remove old resource schemas
        super.cacheObjects(objects);
    }

}
