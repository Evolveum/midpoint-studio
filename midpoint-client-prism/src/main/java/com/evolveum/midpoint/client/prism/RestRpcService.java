package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.RpcService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;

/**
 * // todo implement
 * <p>
 * Created by Viliam Repan (lazyman).
 */
public class RestRpcService<T> implements RpcService<T> {

    @Override
    public ValidateGenerateRpcService validate() {
        return null;
    }

    @Override
    public ValidateGenerateRpcService generate() {
        return null;
    }

    @Override
    public void compare() {

    }

    @Override
    public Post<ExecuteScriptResponseType> executeScript(ExecuteScriptType executeScriptType) {
        return null;
    }

    @Override
    public TaskFuture<T> apost() throws CommonException {
        return null;
    }
}
