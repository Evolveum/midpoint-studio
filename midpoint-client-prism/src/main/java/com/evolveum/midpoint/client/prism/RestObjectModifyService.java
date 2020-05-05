package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectModifyService<O extends ObjectType> implements ObjectModifyService<O> {

    private RestServiceContext context;

    private Class<O> type;

    private String oid;

    public RestObjectModifyService(RestServiceContext context, Class<O> type, String oid) {
        this.context = context;
        this.type = type;
        this.oid = oid;
    }

    @Override
    public ObjectModifyService<O> add(String s, Object o) {
        return null;
    }

    @Override
    public ObjectModifyService<O> add(Map<String, Object> map) {
        return null;
    }

    @Override
    public ObjectModifyService<O> replace(String s, Object o) {
        return null;
    }

    @Override
    public ObjectModifyService<O> replace(Map<String, Object> map) {
        return null;
    }

    @Override
    public ObjectModifyService<O> delete(String s, Object o) {
        return null;
    }

    @Override
    public ObjectModifyService<O> delete(Map<String, Object> map) {
        return null;
    }

    @Override
    public TaskFuture<ObjectReference<O>> apost() throws CommonException {
        return null;
    }
}
