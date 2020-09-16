package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceContext {

    public static final String REST_PREFIX = "/ws/rest";

    public static final MediaType APPLICATION_XML = MediaType.get(javax.ws.rs.core.MediaType.APPLICATION_XML);

    private String url;

    private PrismContext prismContext;

    private OkHttpClient client;

    public ServiceContext(String url, PrismContext prismContext, OkHttpClient client) {
        this.url = url;
        this.prismContext = prismContext;
        this.client = client;
    }

    public PrismContext getPrismContext() {
        return prismContext;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Request.Builder build(String path) {
        return build(path, new HashMap<>());
    }

    public Request.Builder build(String path, Map<String, String> params) {
        HttpUrl.Builder builder = HttpUrl.parse(url + REST_PREFIX + path).newBuilder();

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                builder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        return new Request.Builder().url(builder.build());
    }

    public PrismSerializer<String> getSerializer() {
        return prismContext.xmlSerializer();
    }

    public PrismParser getParser(InputStream entityStream) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(entityStream).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public PrismParser getParser(String entity) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(entity).language(PrismContext.LANG_XML).context(parsingContext);
    }
}
