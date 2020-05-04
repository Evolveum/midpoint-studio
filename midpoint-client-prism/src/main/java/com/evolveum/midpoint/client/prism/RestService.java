package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestService implements Service {

    public static final String REST_PREFIX = "/ws/rest";

    private RestServiceConfiguration configuration;

    private OkHttpClient client;

    private PrismContext prismContext;

    public RestService(RestServiceConfiguration configuration, OkHttpClient client, PrismContext prismContext) {
        this.configuration = configuration;
        this.client = client;
        this.prismContext = prismContext;
    }

    @Override
    public ObjectCollectionService<UserType> users() {
        return collection(UserType.class);
    }

    @Override
    public <T> RpcService<T> rpc() {
        return null;
    }

    @Override
    public ObjectCollectionService<ValuePolicyType> valuePolicies() {
        return collection(ValuePolicyType.class);
    }

    @Override
    public UserType self() throws AuthenticationException {
        Request request = new Request.Builder()
                .url(buildUrl("/self"))
                .build();

        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            return new RestParser(prismContext).read(UserType.class, response.body().byteStream());
        } catch (IOException | SchemaException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Service impersonate(String s) {
        return null;
    }

    @Override
    public Service addHeader(String s, String s1) {
        return null;
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
        return new RestResourceCollectionService();
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
        return new RestShadowCollectionService();
    }

    @Override
    public ObjectCollectionService<RoleType> roles() {
        return collection(RoleType.class);
    }

    @Override
    public ObjectCollectionService<OrgType> orgs() {
        return collection(OrgType.class);
    }

    public <O extends ObjectType> ObjectCollectionService<O> collection(Class<O> type) {
        ObjectTypes ot = ObjectTypes.getObjectType(type);
        return new RestObjectCollectionService<>(ot);
    }

    @Override
    public ServiceUtil util() {
        return new RestServiceUtil(prismContext);
    }

    @Override
    public ScriptingUtil scriptingUtil() {
        return null;
    }

    public String buildUrl(String path) {
        return buildUrl(path, null);
    }

    public String buildUrl(String path, Map<String, String> query) {
        HttpUrl.Builder builder = HttpUrl.parse(configuration.url() + REST_PREFIX + path).newBuilder();
        if (query != null) {
            query.forEach((k, v) -> builder.addQueryParameter(k, v));
        }

        return builder.build().toString();
    }
}
