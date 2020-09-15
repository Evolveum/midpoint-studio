package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface SearchService<O extends ObjectType> {

    SearchResultList<O> list() throws ObjectNotFoundException, AuthenticationException;

    SearchResultList<O> list(ObjectQuery query) throws ObjectNotFoundException, AuthenticationException;

    SearchResultList<O> list(ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options) throws ObjectNotFoundException, AuthenticationException;
}
