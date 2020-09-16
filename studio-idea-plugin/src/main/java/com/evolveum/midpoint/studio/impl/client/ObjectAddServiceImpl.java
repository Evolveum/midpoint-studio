package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, String> options = new HashMap<>();
        opts.forEach(o -> options.put("options", o));

        String path = "/" + ObjectTypes.getRestTypeFromClass(type());

        Request.Builder builder;
        if (object.getOid() != null && StringUtils.isNotEmpty(object.getOid())) {
            path += "/" + object.getOid();

            builder = context().build(path, options)
                    .put(RequestBody.create(object.getContent(), ServiceContext.APPLICATION_XML));
        } else {
            builder = context().build(path, options)
                    .post(RequestBody.create(object.getContent(), ServiceContext.APPLICATION_XML));
        }

        Request req = builder.build();

        OkHttpClient client = context().getClient();
        try (okhttp3.Response response = client.newCall(req).execute()) {
            validateResponse(response);

            ResponseBody body = response.body();
            if (body != null) {
                return body.string();
            }
        } catch (IOException ex) {
            // todo handle exception
            ex.printStackTrace();
        }
        return null;
    }
}
