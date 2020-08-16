package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.PolicyItemsDefinitionBuilder;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestValidateGenerateRpcService implements ValidateGenerateRpcService {

    @Override
    public PolicyItemsDefinitionBuilder items() {
        return null;
    }

    @Override
    public TaskFuture<PolicyItemsDefinitionType> apost() throws CommonException {
        return null;
    }
}
