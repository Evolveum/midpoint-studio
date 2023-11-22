package com.evolveum.midpoint.sdk.api.client;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public interface MidpointClient {

    <O extends ObjectType> MidPointObject get(
            Class<O> type, String oid, ModelExecuteOptionsType options, OperationResult result)
            throws ObjectNotFoundException;

    String add(
            MidPointObject object, ModelExecuteOptionsType options, OperationResult result)
            throws ObjectAlreadyExistsException;

    void modify(
            MidPointObject object, ModelExecuteOptionsType options, OperationResult result)
            throws ObjectNotFoundException;

    <O extends ObjectType> void delete(
            Class<O> type, String oid, DeleteOptions options, OperationResult result)
            throws ObjectNotFoundException;
}
