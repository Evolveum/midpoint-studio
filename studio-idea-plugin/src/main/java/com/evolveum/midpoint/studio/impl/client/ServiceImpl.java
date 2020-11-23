package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.QueryConverter;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointObjectUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.BuildInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NodeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceImpl implements Service {

    private ServiceContext context;

    public ServiceImpl(ServiceContext context) {
        this.context = context;
    }

    @Override
    public ServiceContext context() {
        return context;
    }

    @Override
    public PrismContext prismContext() {
        return context.getPrismContext();
    }

    private Map<String, Object> buildSearchParams(Collection<SelectorOptions<GetOperationOptions>> options, PrismContext prismContext) {
        if (options == null) {
            options = new ArrayList<>();
        }

        Map<String, Object> params = new HashMap<>();
        GetOperationOptions root = SelectorOptions.findRootOptions(options);
        if (root != null && root.getRaw()) {
            params.put("options", "raw");
        }

        for (SelectorOptions o : options) {
            if (!(o.getOptions() instanceof GetOperationOptions)) {
                continue;
            }

            GetOperationOptions goo = (GetOperationOptions) o.getOptions();
            if (goo.getRetrieve() != null) {
                switch (goo.getRetrieve()) {
                    case EXCLUDE:
                        addParameter(params, "exclude", o.getItemPath(prismContext.emptyPath()).toString());
                        break;
                    case INCLUDE:
                        addParameter(params, "include", o.getItemPath(prismContext.emptyPath()).toString());
                        break;
                }
            }
        }

        return params;
    }

    private <O extends ObjectType> Request buildSearchRequest(Class<O> type, ObjectQuery query, Map<String, Object> params) throws SchemaException {
        if (query == null) {
            query = prismContext().queryFactory().createQuery();
        }

        QueryConverter converter = prismContext().getQueryConverter();
        QueryType queryType = converter.createQueryType(query);

        String content = context.serialize(queryType);

        String path = "/" + ObjectTypes.getRestTypeFromClass(type) + "/search";

        Request.Builder builder = context.build(path, params)
                .post(RequestBody.create(content, ServiceContext.APPLICATION_XML));

        return builder.build();
    }

    @Override
    public <O extends ObjectType> SearchResult search(Class<O> type, ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options)
            throws IOException, AuthenticationException {

        Map<String, Object> params = buildSearchParams(options, prismContext());

        try {
            Request req = buildSearchRequest(type, query, params);

            String result = executeRequest(req, String.class);

            List<MidPointObject> objects = MidPointObjectUtils.parseText(result, "Search objects");
            return new SearchResult(objects);
        } catch (SchemaException ex) {
            throw new ClientException("Couldn't create query", ex);
        }
    }

    @Deprecated
    @Override
    public <O extends ObjectType> SearchResultList<O> list(Class<O> type, ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options)
            throws IOException, AuthenticationException {

        Map<String, Object> params = buildSearchParams(options, prismContext());

        try {
            Request req = buildSearchRequest(type, query, params);

            ObjectListType list = executeRequest(req, ObjectListType.class);
            return new SearchResultList<>((List<O>) list.getObject());
        } catch (SchemaException ex) {
            throw new ClientException("Couldn't create query", ex);
        }
    }

    private void addParameter(Map<String, Object> parameters, String param, Object value) {
        Object o = parameters.get(param);
        if (o != null && !(o instanceof List)) {
            return;
        }

        List list = (List) o;
        if (list == null) {
            list = new ArrayList();
            parameters.put(param, list);
        }

        list.add(value);
    }

    @Override
    public String add(MidPointObject object) throws IOException, AuthenticationException {
        return add(object, null);
    }

    @Override
    public String add(MidPointObject object, List<String> opts) throws IOException, AuthenticationException {
        if (opts == null) {
            opts = new ArrayList<>();
        }

        Map<String, Object> options = new HashMap<>();
        opts.forEach(o -> options.put("options", o));

        String path = "/" + ObjectTypes.getRestTypeFromClass(object.getType().getClassDefinition());

        Request.Builder builder;
        if (object.getOid() != null && StringUtils.isNotEmpty(object.getOid())) {
            path += "/" + object.getOid();

            builder = context.build(path, options)
                    .put(RequestBody.create(object.getContent(), ServiceContext.APPLICATION_XML));
        } else {
            builder = context.build(path, options)
                    .post(RequestBody.create(object.getContent(), ServiceContext.APPLICATION_XML));
        }

        Request req = builder.build();

        try {
            return executeRequest(req, String.class);
        } catch (SchemaException ex) {
            // shouldn't happen, there's no parsing involved
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ExecuteScriptResponseType execute(String input) throws IOException, SchemaException, AuthenticationException {
        Request.Builder builder = context.build("/rpc/executeScript")
                .post(RequestBody.create(input, ServiceContext.APPLICATION_XML));

        Request req = builder.build();

        return executeRequest(req, ExecuteScriptResponseType.class);
    }

    private <T> T executeRequest(Request req, Class<T> bodyType) throws IOException, SchemaException, AuthenticationException {
        OkHttpClient client = context.getClient();
        try (Response response = client.newCall(req).execute()) {
            context.validateResponse(response);

            if (bodyType == null) {
                return null;
            }

            if (response.body() == null) {
                return null;
            }

            String body = response.body().string();

            if (String.class.equals(bodyType)) {
                return (T) body;
            }

            PrismParser parser = context.getParser(body);
            return parser.parseRealValue(bodyType);
        }
    }

    @Override
    public TestConnectionResult testServiceConnection() {
        Request.Builder builder = context.build("/" + ObjectTypes.NODE.getRestType() + "/current")
                .get();

        Request req = builder.build();

        try {
            NodeType node = executeRequest(req, NodeType.class);
            if (node == null) {
                return new TestConnectionResult(false, new IllegalStateException("No response body received"));
            }

            BuildInformationType build = node.getBuild();

            return new TestConnectionResult(true, build.getVersion(), build.getRevision());
        } catch (Exception ex) {
            return new TestConnectionResult(false, ex);
        }
    }

    @Override
    public <O extends ObjectType> MidPointObject get(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException {

        return get(type, oid, null);
    }

    @Override
    public <O extends ObjectType> MidPointObject get(Class<O> type, String oid, Collection<SelectorOptions<GetOperationOptions>> options)
            throws ObjectNotFoundException, AuthenticationException, IOException {

        return executeGet(type, oid, options);
    }

    private <O extends ObjectType> MidPointObject executeGet(Class<O> type, String oid, Collection<SelectorOptions<GetOperationOptions>> options)
            throws ObjectNotFoundException, AuthenticationException, IOException {

        if (options == null) {
            options = new ArrayList<>();
        }

        Map<String, Object> params = new HashMap<>();
        GetOperationOptions root = SelectorOptions.findRootOptions(options);
        if (root != null && root.getRaw()) {
            params.put("options", "raw");
        }

        String path = "/" + ObjectTypes.getRestTypeFromClass(type) + "/" + oid;
        Request.Builder builder = context.build(path, params)
                .get();

        Request req = builder.build();

        OkHttpClient client = context.getClient();
        try (Response response = client.newCall(req).execute()) {
            validateResponseCode(response, oid);

            if (javax.ws.rs.core.Response.Status.OK.getStatusCode() != response.code()) {
                throw new ClientException("Unknown response status: " + response.code(), context.getOperationResultFromResponse(response));
            }

            if (response.body() == null) {
                return null;
            }

            String content = response.body().string();
            List<MidPointObject> objects = MidPointObjectUtils.parseText(content, null);

            if (objects.size() == 1) {
                return objects.get(0);
            }

            return null;
        }
    }

    @Override
    public <O extends ObjectType> void delete(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException {

        delete(type, oid, new DeleteOptions());
    }

    @Override
    public <O extends ObjectType> void delete(Class<O> type, String oid, DeleteOptions opts)
            throws ObjectNotFoundException, AuthenticationException, IOException {

        if (opts == null) {
            opts = new DeleteOptions();
        }

        Map<String, Object> options = new HashMap<>();
        if (opts.raw()) {
            options.put("options", "raw");
        }

        String path = ObjectTypes.getRestTypeFromClass(type);

        Request.Builder builder = context.build("/" + path + "/" + oid, options)
                .delete();

        Request req = builder.build();

        OkHttpClient client = context.getClient();
        try (okhttp3.Response response = client.newCall(req).execute()) {
            validateResponseCode(response, oid);
        }
    }

    @Override
    public OperationResult testResourceConnection(String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException, SchemaException {

        String path = "/" + ObjectTypes.RESOURCE.getRestType() + "/" + oid + "/test";

        Request.Builder builder = context.build(path)
                .post(RequestBody.create("", ServiceContext.APPLICATION_XML));

        Request req = builder.build();

        OkHttpClient client = context.getClient();
        try (okhttp3.Response response = client.newCall(req).execute()) {
            validateResponseCode(response, oid);

            if (javax.ws.rs.core.Response.Status.OK.getStatusCode() != response.code()) {
                throw new ClientException("Unknown response status: " + response.code(), context.getOperationResultFromResponse(response));
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }

            PrismParser parser = context.getParser(body.string());
            OperationResultType result = parser.parseRealValue(OperationResultType.class);

            return result != null ? OperationResult.createOperationResult(result) : null;
        }
    }

    private void validateResponseCode(okhttp3.Response response, String oid) throws ObjectNotFoundException, AuthenticationException {
        if (javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode() == response.code()) {
            throw new ObjectNotFoundException("Cannot get object with oid '" + oid + "'. Object doesn't exist");
        }

        if (javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode() == response.code()) {
            throw new AuthenticationException(javax.ws.rs.core.Response.Status.fromStatusCode(response.code()).getReasonPhrase());
        }
    }
}
