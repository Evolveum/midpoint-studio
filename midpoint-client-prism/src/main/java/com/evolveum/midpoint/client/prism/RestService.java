package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestService implements Service {

    public static final String IMPERSONATE_HEADER = "Switch-To-Principal";

    private RestServiceContext context;

    public RestService(RestServiceContext context) {
        this.context = context;
    }

    @Override
    public ObjectCollectionService<UserType> users() {
        return collection(UserType.class);
    }

    @Override
    public <T> RpcService<T> rpc() {
        return new RestRpcService<>();
    }

    @Override
    public ObjectCollectionService<ValuePolicyType> valuePolicies() {
        return collection(ValuePolicyType.class);
    }

    @Override
    public UserType self() throws AuthenticationException {
        Request request = new Request.Builder()
                .url(context.buildUrl("/self"))
                .build();

        Call call = context.client().newCall(request);

        try (Response response = call.execute()) {
            return new RestParser(context.prismContext()).read(UserType.class, response.body().byteStream());
        } catch (IOException | SchemaException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Service impersonate(String name) {
        return addHeader(IMPERSONATE_HEADER, name);
    }

    @Override
    public Service addHeader(String name, String value) {
        OkHttpClient.Builder builder = context.client().newBuilder();
        builder.addInterceptor(chain -> {

            Request request = chain.request();
            Request newRequest;

            newRequest = request.newBuilder()
                    .addHeader(name, value)
                    .build();

            return chain.proceed(newRequest);
        });

        RestServiceContext context = new RestServiceContext(this.context.configuration(), builder.build(),
                this.context.prismContext());

        return new RestService(context);
    }

    @Override
    public ObjectCollectionService<SecurityPolicyType> securityPolicies() {
        return collection(SecurityPolicyType.class);
    }

    @Override
    public ObjectCollectionService<ConnectorType> connectors() {
        return collection(ConnectorType.class);
    }

    @Override
    public ObjectCollectionService<ConnectorHostType> connectorHosts() {
        return collection(ConnectorHostType.class);
    }

    @Override
    public ObjectCollectionService<GenericObjectType> genericObjects() {
        return collection(GenericObjectType.class);
    }

    @Override
    public ResourceCollectionService resources() {
        return new RestResourceCollectionService(context);
    }

    @Override
    public ObjectCollectionService<ObjectTemplateType> objectTemplates() {
        return collection(ObjectTemplateType.class);
    }

    @Override
    public ObjectCollectionService<SystemConfigurationType> systemConfigurations() {
        return collection(SystemConfigurationType.class);
    }

    @Override
    public ObjectCollectionService<TaskType> tasks() {
        return collection(TaskType.class);
    }

    @Override
    public ShadowCollectionService shadows() {
        return new RestShadowCollectionService(context);
    }

    @Override
    public ObjectCollectionService<RoleType> roles() {
        return collection(RoleType.class);
    }

    @Override
    public ObjectCollectionService<OrgType> orgs() {
        return collection(OrgType.class);
    }

    @Override
    public ServiceUtil util() {
        return new RestServiceUtil(context.prismContext());
    }

    @Override
    public ScriptingUtil scriptingUtil() {
        return new RestScriptingUtil();
    }

    public <O extends ObjectType> ObjectCollectionService<O> collection(Class<O> type) {
        if (ResourceType.class.equals(type)) {
            return (ObjectCollectionService<O>) new RestResourceCollectionService(context);
        } else if (ShadowType.class.equals(type)) {
            return (ObjectCollectionService<O>) new RestShadowCollectionService(context);
        }

        return new RestObjectCollectionService<O>(context, type);
    }

    public TestConnectionResult testConnection() {
        String path = "/" + ObjectTypes.NODE.getRestType() + "/current";

        Request request = new Request.Builder()
                .url(context.buildUrl(path))
                .build();

        Call call = context.client().newCall(request);

        try (Response response = call.execute()) {
            NodeType node = new RestParser(context.prismContext()).read(NodeType.class, response.body().byteStream());
            BuildInformationType build = node.getBuild();

            return new TestConnectionResult(true, build.getVersion(), build.getRevision());
        } catch (IOException | SchemaException ex) {
            return new TestConnectionResult(false, ex);
        }
    }
}