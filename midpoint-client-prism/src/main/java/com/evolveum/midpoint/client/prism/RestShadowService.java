package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ShadowService;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestShadowService extends RestObjectService<ShadowType> implements ShadowService {

    public RestShadowService(RestServiceContext context, String oid) {
        super(context, ShadowType.class, oid);
    }

    @Override
    public OperationResultType importShadow() throws ObjectNotFoundException {
        return null;
    }

    @Override
    public FocusType owner() throws ObjectNotFoundException {
        return null;
    }
}
