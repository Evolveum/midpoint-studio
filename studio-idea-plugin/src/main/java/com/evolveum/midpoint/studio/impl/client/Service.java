package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

    ExecuteScriptResponseType execute(String input)
            throws IOException, SchemaException, AuthenticationException;

    <O extends ObjectType> MidPointObject get(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> MidPointObject get(Class<O> type, String oid, Collection<SelectorOptions<GetOperationOptions>> options)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> void delete(Class<O> type, String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    <O extends ObjectType> void delete(Class<O> type, String oid, DeleteOptions options)
            throws ObjectNotFoundException, AuthenticationException, IOException;

    OperationResult testResourceConnection(String oid)
            throws ObjectNotFoundException, AuthenticationException, IOException, SchemaException;

    ServiceContext context();

    TestConnectionResult testServiceConnection();

    PrismContext prismContext();
}
