package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectCredentialService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteCredentialResetRequestType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectCredentialService implements ObjectCredentialService {

    @Override
    public ObjectCredentialService executeResetPassword(ExecuteCredentialResetRequestType executeCredentialResetRequestType) {
        return null;
    }

    @Override
    public TaskFuture apost() throws CommonException {
        return null;
    }
}
