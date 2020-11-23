package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectAddService;
import com.evolveum.midpoint.client.api.ObjectCollectionService;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectCollectionService<O extends ObjectType> implements ObjectCollectionService<O> {

    private RestServiceContext context;

    private Class<O> type;

    public RestObjectCollectionService(RestServiceContext context, Class<O> type) {
        this.context = context;
        this.type = type;
    }

    protected RestServiceContext context() {
        return context;
    }

    @Override
    public ObjectService<O> oid(String oid) {
        return new RestObjectService<>(context, type, oid);
    }

    @Override
    public SearchService<O> search() {
        return null;
    }

    @Override
    public ObjectAddService<O> add(O o) {
        return null;
    }
}
