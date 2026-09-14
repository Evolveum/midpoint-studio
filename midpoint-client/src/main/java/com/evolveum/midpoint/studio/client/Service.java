package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.smart.api.info.AiInfo;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Service {

    @Deprecated
    <O extends ObjectType> SearchResultList<O> list(Class<O> type, ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options)
            throws IOException, AuthenticationException;

    <O extends ObjectType> SearchResult search(Class<O> type, ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options)
            throws IOException, AuthenticationException;

    String add(MidPointObject object)
            throws IOException, AuthenticationException;

    String add(MidPointObject object, List<String> opts)
            throws IOException, AuthenticationException;

    String modify(MidPointObject object, List<String> opts)
            throws IOException, AuthenticationException;

    <T extends ObjectType> T upsert(MidPointObject object, List<String> opts)
            throws IOException, AuthenticationException, SchemaException;

    ExecuteScriptResponseType execute(String input)
            throws IOException, SchemaException, AuthenticationException;

    <O extends ObjectType> MidPointObject get(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> MidPointObject get(Class<O> type, String oid, Collection<SelectorOptions<GetOperationOptions>> options)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> OperationResult recompute(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException, SchemaException;

    <O extends ObjectType> void delete(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> void delete(Class<O> type, String oid, DeleteOptions options)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    OperationResult testResourceConnection(String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException, SchemaException;

    /**
     * Fetches a fragment of the server log file. Both parameters are mandatory by design:
     * omitting fromPosition server-side means "from offset 0" (the whole file), omitting
     * maxSize means "to EOF". Negative fromPosition is an offset from EOF (tail).
     */
    LogFileContent getLog(long fromPosition, long maxSize)
            throws IOException, AuthenticationException;

    long getLogFileSize()
            throws IOException, AuthenticationException;

    ServiceContext context();

    TestConnectionResult testServiceConnection();

    PrismContext prismContext();

    Map<SchemaFileType, String> getExtensionSchemas() throws IOException, SchemaException, AuthenticationException, ClientException;

    <C> C submitOperationSmartIntegration(RPCOperation rpcOperation, Map<String, Object> params, String requestBodyContent) throws SchemaException, AuthenticationException, IOException;

    <C> C getStatusInfoSmartIntegration(RPCOperation rpcOperation, Map<String, Object> params) throws SchemaException, AuthenticationException, IOException;

    <C> C submitOperationConnGenerator(RPCOperation rpcOperation, Map<String, Object> params, String requestBodyContent) throws SchemaException, AuthenticationException, IOException;

    <C> C getStatusInfoConnGenerator(RPCOperation rpcOperation, Map<String, Object> params) throws SchemaException, AuthenticationException, IOException;

    AiInfoType getAiInfo() throws ClientException, SchemaException, AuthenticationException, IOException;

    File downloadConnector(@NotNull String bundleName) throws ClientException, SchemaException, AuthenticationException, IOException;
}
