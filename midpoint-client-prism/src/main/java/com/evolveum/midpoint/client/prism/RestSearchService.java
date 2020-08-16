package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.query.FilterEntryOrEmpty;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestSearchService<O extends ObjectType> implements SearchService<O> {

    @Override
    public FilterEntryOrEmpty<O> queryFor(Class<O> aClass) {
        return new RestFilterEntryOrEmpty<>();
    }

    @Override
    public SearchResult<O> get() throws ObjectNotFoundException {
        return null;
    }
}
