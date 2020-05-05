package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ObjectCredentialService;
import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectService<O extends ObjectType> implements ObjectService<O> {

    private RestServiceContext context;

    private Class<O> type;

    private String oid;

    public RestObjectService(RestServiceContext context, Class<O> type, String oid) {
        this.context = context;
        this.type = type;
        this.oid = oid;
    }

    @Override
    public O get(List<String> options) throws ObjectNotFoundException {
        return get(options, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public O get(List<String> options, List<String> include, List<String> exclude) throws ObjectNotFoundException {
        // todo options + error handling

        String path = "/" + ObjectTypes.getObjectType(type).getRestType() + "/" + oid;

        Request request = new Request.Builder()
                .url(context.buildUrl(path))
                .build();

        Call call = context.client().newCall(request);

        try (Response response = call.execute()) {
            return new RestParser(context.prismContext()).read(type, response.body().byteStream());
        } catch (IOException | SchemaException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ObjectModifyService<O> modify() throws ObjectNotFoundException {
        return new RestObjectModifyService<>(context, type, oid);
    }

    @Override
    public ObjectCredentialService<O> credential() {
        return null;
    }

    @Override
    public ValidateGenerateRpcService generate() {
        return null;
    }

    @Override
    public ValidateGenerateRpcService validate() {
        return null;
    }

    @Override
    public void delete() throws ObjectNotFoundException {

    }

    @Override
    public O get() throws ObjectNotFoundException {
        return get(Collections.emptyList());
    }
}
