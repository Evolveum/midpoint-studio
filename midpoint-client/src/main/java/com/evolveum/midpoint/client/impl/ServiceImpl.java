package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.BuildInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceImpl implements Service<WebClient> {

    private ServiceContext context;

    public ServiceImpl(ServiceContext context) {
        this.context = context;
    }

    @Override
    public WebClient getClient() {
        return context.getClient();
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
    public ExecuteScriptResponseType execute(Object input) throws AuthenticationException {
        WebClient client = context.getClient();

        client = client.replacePath(CommonService.REST_PREFIX + "/rpc/executeScript");
        Response response = client.post(input);

        CommonService.validateResponse(response);

        return response.readEntity(ExecuteScriptResponseType.class);
    }

    @Override
    public TestConnectionResult testConnection() throws AuthenticationException {
        String path = "/" + ObjectTypes.NODE.getRestType() + "/current";

        WebClient client = context.getClient();
        client = client.replacePath(CommonService.REST_PREFIX + path);

        try {
            Response response = client.get();

            CommonService.validateResponse(response);

            NodeType node = response.readEntity(NodeType.class);
            BuildInformationType build = node.getBuild();

            return new TestConnectionResult(true, build.getVersion(), build.getRevision());
        } catch (Exception ex) {
            return new TestConnectionResult(false, ex);
        }
    }
}
