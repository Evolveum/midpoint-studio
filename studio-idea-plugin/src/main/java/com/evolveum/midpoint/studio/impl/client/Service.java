package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Service {

    <O extends ObjectType> SearchService<O> search(Class<O> type);

    <O extends ObjectType> ObjectService<O> oid(Class<O> type, String oid);

    ObjectAddService add(MidPointObject object);

    ExecuteScriptResponseType execute(Object input) throws AuthenticationException;

    TestConnectionResult testConnection() throws AuthenticationException;

    PrismContext prismContext();
}
