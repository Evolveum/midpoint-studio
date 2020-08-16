package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.CommonException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface ObjectService<O extends ObjectType> {

    O get() throws ObjectNotFoundException, AuthenticationException;

    O get(Collection<SelectorOptions<GetOperationOptions>> options) throws ObjectNotFoundException, AuthenticationException;

    void modify(ObjectDelta<O> delta) throws CommonException;

    void delete() throws ObjectNotFoundException, AuthenticationException;

    void delete(DeleteOptions options) throws ObjectNotFoundException, AuthenticationException;

    OperationResult testConnection() throws ObjectNotFoundException, AuthenticationException;
}
