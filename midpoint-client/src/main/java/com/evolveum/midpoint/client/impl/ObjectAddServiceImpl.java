package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.AddOptions;
import com.evolveum.midpoint.client.api.AuthenticationException;
import com.evolveum.midpoint.client.api.ObjectAddService;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectAddServiceImpl<O extends ObjectType> extends CommonService<O> implements ObjectAddService<O> {

    private O object;

    public ObjectAddServiceImpl(ServiceContext context, O object) {
        super(context, (Class) object.getClass());

        this.object = object;
    }

    @Override
    public String add() throws AuthenticationException {
        return add(null);
    }

    @Override
    public String add(AddOptions opts) throws AuthenticationException {
        if (opts == null) {
            opts = new AddOptions();
        }

        WebClient client = client();

        String path = ObjectTypes.getRestTypeFromClass(type());
        if (opts.overwrite() && object.getOid() != null) {
            path += "/" + object.getOid();
        }

        client.replacePath(REST_PREFIX + "/" + path);

        Response response = opts.overwrite() ? client.put(object) : client.post(object);
        validateResponse(response);

        return response.readEntity(String.class);
    }
}
