package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceImpl implements Service {

    private ServiceContext context;

    public ServiceImpl(ServiceContext context) {
        this.context = context;
    }

    @Override
    public <O extends ObjectType> SearchService<O> search(Class<O> type) {
        return new SearchServiceImpl<>(context, type);
    }

    @Override
    public <O extends ObjectType> ObjectService<O> oid(Class<O> type, String oid) {
        return new ObjectServiceImpl<>(context, type, oid);
    }

    @Override
    public <O extends ObjectType> ObjectAddService<O> add(O object) {
        return new ObjectAddServiceImpl<>(context, object);
    }

    @Override
    public PrismContext prismContext() {
        return context.getPrismContext();
    }

    @Override
    public Object execute(Object input) throws AuthenticationException  {
        WebClient client = context.getClient();

        client = client.replacePath(CommonService.REST_PREFIX + "/rpc/executeScript");
        Response response = client.post(input);

        CommonService.validateResponse(response);

        return response.readEntity(Object.class);
    }
}
