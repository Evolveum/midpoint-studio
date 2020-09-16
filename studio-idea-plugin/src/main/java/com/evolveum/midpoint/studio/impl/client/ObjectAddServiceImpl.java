package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectAddServiceImpl<O extends ObjectType> extends CommonService<O> implements ObjectAddService<O> {

    private MidPointObject object;

    public ObjectAddServiceImpl(ServiceContext context, MidPointObject object) {
        super(context, (Class<O>) object.getType().getClassDefinition());

        this.object = object;
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

        OkHttpClient client = client();

        Response response;
        String path = ObjectTypes.getRestTypeFromClass(type());
        if (object.getOid() != null && StringUtils.isNotEmpty(object.getOid())) {
            path += "/" + object.getOid();

            client = client.replacePath(REST_PREFIX + "/" + path).replaceQuery(query.toString());
            response = client.put(object.getContent());
        } else {
            client = client.replacePath(REST_PREFIX + "/" + path).replaceQuery(query.toString());
            response = client.post(object.getContent());
        }

        validateResponse(response);

        if (!response.hasEntity()) {
            return null;
        }

        return response.readEntity(String.class);
    }
}
