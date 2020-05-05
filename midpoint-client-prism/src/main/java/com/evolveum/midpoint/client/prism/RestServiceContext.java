package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.prism.PrismContext;
import okhttp3.OkHttpClient;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestServiceContext {

    private RestServiceConfiguration configuration;

    private OkHttpClient client;

    private PrismContext prismContext;

    public RestServiceContext(RestServiceConfiguration configuration, OkHttpClient client, PrismContext prismContext) {
        this.configuration = configuration;
        this.client = client;
        this.prismContext = prismContext;
    }

    public RestServiceConfiguration configuration() {
        return configuration;
    }

    public OkHttpClient client() {
        return client;
    }

    public PrismContext prismContext() {
        return prismContext;
    }
}
