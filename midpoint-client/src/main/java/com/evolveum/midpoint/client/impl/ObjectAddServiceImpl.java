package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.AuthenticationException;
import com.evolveum.midpoint.client.api.ObjectAddService;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectAddServiceImpl<O extends ObjectType> extends CommonService<O> implements ObjectAddService<O> {

    private O object;

    private String objectRaw;

    public ObjectAddServiceImpl(ServiceContext context, O object) {
        super(context, (Class) object.getClass());

        this.object = object;
    }

    public ObjectAddServiceImpl(ServiceContext context, Class<O> type, String objectRaw) {
        super(context, type);
        this.objectRaw = objectRaw;
    }

    @Override
    public String execute() throws AuthenticationException {
        return execute(null);
    }

    @Override
    public String execute(List<String> opts) throws AuthenticationException {
        if (opts == null) {
            opts = new ArrayList<>();
        }

        StringBuilder query = new StringBuilder();
        opts.forEach(o -> query.append("options=").append(o).append("&"));

        WebClient client = client();

        Response response;
        String path = ObjectTypes.getRestTypeFromClass(type());
        if (object.getOid() != null && StringUtils.isNotEmpty(object.getOid())) {
            path += "/" + object.getOid();

            client = client.replacePath(REST_PREFIX + "/" + path).replaceQuery(query.toString());
            response = client.put(object.asPrismObject());
        } else {
            client = client.replacePath(REST_PREFIX + "/" + path).replaceQuery(query.toString());
            response = client.post(object.asPrismObject());
        }

        validateResponse(response);

        return response.readEntity(String.class);
    }
}
