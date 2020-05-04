package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectAddService;
import com.evolveum.midpoint.client.api.ObjectCollectionService;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectCollectionService<O extends ObjectType> implements ObjectCollectionService<O> {

    private ObjectTypes type;

    public RestObjectCollectionService(ObjectTypes type) {
        this.type = type;
    }

    @Override
    public ObjectService<O> oid(String s) {
        return null;
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
