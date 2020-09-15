package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class CommonService<O extends ObjectType> {

    public static final String REST_PREFIX = "/ws/rest";

    private ServiceContext context;

    private Class<O> type;

    public CommonService(ServiceContext context, Class<O> type) {
        this.context = context;
        this.type = type;
    }

    public Class<O> type() {
        return type;
    }

    protected PrismContext prismContext() {
        return context.getPrismContext();
    }

    protected WebClient client() {
        return context.getClient();
    }

    public static void validateResponse(Response response) throws AuthenticationException {
        Response.StatusType info = response.getStatusInfo();

        if (Response.Status.UNAUTHORIZED.getStatusCode() == info.getStatusCode()) {
            throw new AuthenticationException(info.getReasonPhrase());
        }

        if (!Response.Status.Family.SUCCESSFUL.equals(info.getFamily())) {
            OperationResult result = null;
            try {
                OperationResultType resultType = response.readEntity(OperationResultType.class);
                result = resultType != null ? OperationResult.createOperationResult(resultType) : null;
            } catch (Exception ex) {
            }

            throw new ClientException("Unknown response status: " + info.getStatusCode() + ", reason: " + info.getReasonPhrase(), result);
        }
    }
}
