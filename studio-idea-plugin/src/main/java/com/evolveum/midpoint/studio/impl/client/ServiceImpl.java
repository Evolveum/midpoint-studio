package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.BuildInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    public ObjectAddService add(MidPointObject object) {
        return new ObjectAddServiceImpl(context, object);
    }

    @Override
    public PrismContext prismContext() {
        return context.getPrismContext();
    }

    @Override
    public ExecuteScriptResponseType execute(Object input) throws AuthenticationException {
        String content = null; // todo convert

        Request.Builder builder = context.build("/rpc/executeScript")
                .post(RequestBody.create(content, ServiceContext.APPLICATION_XML));

        Request req = builder.build();

        OkHttpClient client = context.getClient();
        try (Response response = client.newCall(req).execute()) {
            CommonService.validateResponse(response);

            String body = response.body().string();
            PrismParser parser = context.getParser(body);
            return parser.parseRealValue(ExecuteScriptResponseType.class);
            // body to ExecuteScriptResponseType
        } catch (Exception ex) {
            // todo
        }
        // todo
        return null;
    }

    @Override
    public TestConnectionResult testConnection() throws AuthenticationException {
        Request.Builder builder = context.build("/" + ObjectTypes.NODE.getRestType() + "/current")
                .get();

        Request req = builder.build();

        OkHttpClient client = context.getClient();
        try (Response response = client.newCall(req).execute()) {
            CommonService.validateResponse(response);

            String body = response.body().string();
            // body to NodeType

            // todo
            NodeType node = null;//response.readEntity(NodeType.class);
            BuildInformationType build = node.getBuild();

            return new TestConnectionResult(true, build.getVersion(), build.getRevision());
        } catch (Exception ex) {
            return new TestConnectionResult(false, ex);
        }
    }
}
