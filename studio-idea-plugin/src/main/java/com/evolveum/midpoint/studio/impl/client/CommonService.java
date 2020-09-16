package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import okhttp3.OkHttpClient;
import okhttp3.Response;


/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class CommonService<O extends ObjectType> {

    private ServiceContext context;

    private Class<O> type;

    public CommonService(ServiceContext context, Class<O> type) {
        this.context = context;
        this.type = type;
    }

    public ServiceContext context() {
        return this.context;
    }

    public Class<O> type() {
        return type;
    }

    protected PrismContext prismContext() {
        return context.getPrismContext();
    }

    protected OkHttpClient client() {
        return context.getClient();
    }

    public void validateResponse(Response response) throws AuthenticationException {
        context.validateResponse(response);
    }
}
