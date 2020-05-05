package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ResourceCollectionService;
import com.evolveum.midpoint.client.api.ResourceService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestResourceCollectionService extends RestObjectCollectionService<ResourceType>
        implements ResourceCollectionService {

    public RestResourceCollectionService(RestServiceContext context) {
        super(context, ResourceType.class);
    }

    @Override
    public ResourceService oid(String oid) {
        return new RestResourceService(context(), oid);
    }
}
