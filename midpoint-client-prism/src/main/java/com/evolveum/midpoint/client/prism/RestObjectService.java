package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectCredentialService;
import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectService<O extends ObjectType> implements ObjectService<O> {

    private RestServiceContext context;

    private Class<O> type;

    private String oid;

    public RestObjectService(RestServiceContext context, Class<O> type, String oid) {
        this.context = context;
        this.type = type;
        this.oid = oid;
    }

    @Override
    public O get(List<String> list) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public O get(List<String> list, List<String> list1, List<String> list2) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ObjectModifyService<O> modify() throws ObjectNotFoundException {
        return new RestObjectModifyService<>(context, type, oid);
    }

    @Override
    public ObjectCredentialService<O> credential() {
        return null;
    }

    @Override
    public ValidateGenerateRpcService generate() {
        return null;
    }

    @Override
    public ValidateGenerateRpcService validate() {
        return null;
    }

    @Override
    public void delete() throws ObjectNotFoundException {

    }

    @Override
    public O get() throws ObjectNotFoundException {
        return null;
    }
}
