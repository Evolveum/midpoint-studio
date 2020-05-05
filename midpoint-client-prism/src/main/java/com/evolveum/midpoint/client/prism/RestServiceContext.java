package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.prism.PrismContext;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestServiceContext {

    public static final String REST_PREFIX = "/ws/rest";

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

    public String buildUrl(String path) {
        return buildUrl(path, null);
    }

    public String buildUrl(String path, Map<String, String> query) {
        HttpUrl.Builder builder = HttpUrl.parse(configuration.url() + REST_PREFIX + path).newBuilder();
        if (query != null) {
            query.forEach((k, v) -> builder.addQueryParameter(k, v));
        }

        return builder.build().toString();
    }
}
