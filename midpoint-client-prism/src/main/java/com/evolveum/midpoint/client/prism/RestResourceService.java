package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ResourceService;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestResourceService extends RestObjectService<ResourceType> implements ResourceService {

    public RestResourceService(RestServiceContext context, String oid) {
        super(context, ResourceType.class, oid);
    }

    @Override
    public OperationResultType test() throws ObjectNotFoundException {
        return null;
    }
}
