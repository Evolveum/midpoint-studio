package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ShadowCollectionService;
import com.evolveum.midpoint.client.api.ShadowService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestShadowCollectionService extends RestObjectCollectionService<ShadowType>
        implements ShadowCollectionService {

    public RestShadowCollectionService(RestServiceContext context) {
        super(context, ShadowType.class);
    }

    @Override
    public ShadowService oid(String oid) {
        return new RestShadowService(context(), oid);
    }
}
