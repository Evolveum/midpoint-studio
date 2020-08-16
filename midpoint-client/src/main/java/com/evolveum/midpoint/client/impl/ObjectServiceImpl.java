package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.AuthenticationException;
import com.evolveum.midpoint.client.api.DeleteOptions;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.CommonException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import org.apache.commons.lang.Validate;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectServiceImpl<O extends ObjectType> extends CommonService<O> implements ObjectService<O> {

    private String oid;

    public ObjectServiceImpl(ServiceContext context, Class<O> type, String oid) {
        super(context, type);

        Validate.notNull(oid, "Oid must not be null");
        this.oid = oid;
    }

    @Override
    public O get() throws ObjectNotFoundException, AuthenticationException {
        return get(null);
    }

    @Override
    public O get(Collection<SelectorOptions<GetOperationOptions>> options) throws ObjectNotFoundException, AuthenticationException {
        if (options == null) {
            options = new ArrayList<>();
        }

        // todo use options

        GetOperationOptions rootOptions = SelectorOptions.findRootOptions(options);

        String query = null;
        if (GetOperationOptions.isRaw(rootOptions)) {
            query = "options=raw";
        }

        WebClient client = client();

        String path = ObjectTypes.getRestTypeFromClass(type());
        client.replacePath(REST_PREFIX + "/" + path + "/" + oid);

        Response response = client.get();

        validateResponseCode(response);

        if (Response.Status.OK.getStatusCode() != response.getStatus()) {
            throw new SystemException("Unknown response status: " + response.getStatus());
        }

        return response.readEntity(type());
    }

    @Override
    public void modify(ObjectDelta<O> delta) throws CommonException {
        // todo implement
    }

    @Override
    public void delete() throws ObjectNotFoundException, AuthenticationException {
        delete(new DeleteOptions());
    }

    @Override
    public void delete(DeleteOptions options) throws ObjectNotFoundException, AuthenticationException {
        if (options == null) {
            options = new DeleteOptions();
        }

        String opts = null;
        if (options.raw()) {
            opts = "options=raw";
        }

        WebClient client = client();

        String path = ObjectTypes.getRestTypeFromClass(type());
        client = client.replacePath(REST_PREFIX + "/" + path + "/" + oid).replaceQuery(opts);

        Response response = client.delete();

        validateResponseCode(response);
    }

    @Override
    public OperationResult testConnection() throws ObjectNotFoundException, AuthenticationException {
        if (!ResourceType.class.equals(type())) {
            throw new IllegalStateException("Can't call testConnection operation on non ResourceType object");
        }

        WebClient client = client();

        String path = ObjectTypes.getRestTypeFromClass(type());
        client.replacePath(REST_PREFIX + "/" + path + "/" + oid + "/test");

        Response response = client.post(null);

        validateResponseCode(response);

        if (Response.Status.OK.getStatusCode() != response.getStatus()) {
            throw new SystemException("Unknown response status: " + response.getStatus());
        }

        OperationResultType res = response.readEntity(OperationResultType.class);
        return res != null ? OperationResult.createOperationResult(res) : null;
    }

    private void validateResponseCode(Response response) throws ObjectNotFoundException, AuthenticationException {
        if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new ObjectNotFoundException("Cannot get object with oid '" + oid + "'. Object doesn't exist");
        }

        if (Response.Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new AuthenticationException(response.getStatusInfo().getReasonPhrase());
        }
    }
}
