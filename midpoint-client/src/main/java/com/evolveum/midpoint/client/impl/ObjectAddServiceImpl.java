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
    public String post() throws AuthenticationException {
        return post(null);
    }

    @Override
    public String post(AddOptions opts) throws AuthenticationException {
        if (opts == null) {
            opts = new AddOptions();
        }

        WebClient client = client();

        String path = ObjectTypes.getRestTypeFromClass(type());
        client.replacePath(REST_PREFIX + "/" + path);

        Response response = client.post(object);
        validateResponse(response);

        return response.readEntity(String.class);
    }
}
