package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
            throws IOException, AuthenticationException;

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

    ServiceContext context();

    TestConnectionResult testServiceConnection();

    PrismContext prismContext();

    Map<SchemaFileType, String> getExtensionSchemas() throws IOException, SchemaException, AuthenticationException, ClientException;

    String submitOperationSuggestionObjectType(@NotNull String oid, QName objectClass) throws ClientException, SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoSuggestionObjectType(@NotNull String token) throws ClientException, SchemaException, AuthenticationException, IOException;

    String submitOperationSuggestionCorrelation(@NotNull String oid, String kind, String intent) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoSuggestionCorrelation(@NotNull String token) throws ClientException, SchemaException, AuthenticationException, IOException;

    String submitOperationSuggestionMapping(@NotNull String oid, String kind, String intent, boolean isInbound) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoSuggestionMapping(@NotNull String token) throws ClientException, SchemaException, AuthenticationException, IOException;

    String submitOperationSuggestionAssociation(@NotNull String oid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoSuggestionAssociation(@NotNull String token) throws ClientException, SchemaException, AuthenticationException, IOException;

    File downloadConnector(@NotNull String bundleName) throws SchemaException, AuthenticationException, IOException;

    ConnectorDevelopmentType continueFrom(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    String submitOperationCreateConnector(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoCreateConnector(@NotNull String token) throws SchemaException, AuthenticationException, IOException;

    String submitOperationDiscoverBasicInformation(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoDiscoverBasicInformation(@NotNull String token) throws SchemaException, AuthenticationException, IOException;

    String submitOperationDiscoverDocumentation(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoDiscoverDocumentation(@NotNull String token) throws SchemaException, AuthenticationException, IOException;

    String submitOperationProcessDocumentation(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoProcessDocumentation(@NotNull String token) throws SchemaException, AuthenticationException, IOException;

    String submitOperationGenerateAuthenticationScript(@NotNull String connectorDevelopmentOid) throws SchemaException, AuthenticationException, IOException;

    @Nullable SmartIntegrationOperationStatusInfoType getStatusInfoGenerateArtifact(@NotNull String token) throws SchemaException, AuthenticationException, IOException;
}
