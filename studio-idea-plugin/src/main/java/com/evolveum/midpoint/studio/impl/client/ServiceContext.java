package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import okhttp3.*;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

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

    public Request.Builder build(String path, Map<String, Object> params) {
        HttpUrl.Builder builder = HttpUrl.parse(url + REST_PREFIX + path).newBuilder();

        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                Object value = param.getValue();
                if (!(value instanceof List)) {
                    builder.addQueryParameter(param.getKey(), param.getValue().toString());
                } else {
                    List list = (List) value;
                    list.forEach(v -> builder.addQueryParameter(param.getKey(), v.toString()));
                }
            }
        }

        return new Request.Builder().url(builder.build());
    }

    public PrismSerializer<String> getSerializer() {
        return prismContext.xmlSerializer()
                .options(SerializationOptions.createSerializeReferenceNames());
    }

    public String serialize(Object object) throws SchemaException {
        final QName fakeQName = new QName(PrismConstants.NS_TYPES, "object");

        PrismSerializer<String> serializer = getSerializer();

        String result;
        if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            result = serializer.serialize(ot.asPrismObject());
        } else if (object instanceof PrismObject) {
            result = serializer.serialize((PrismObject<?>) object);
        } else if (object instanceof OperationResult) {
            LocalizationService localizationService = new LocalizationServiceImpl();
            Function<LocalizableMessage, String> resolveKeys = msg -> localizationService.translate(msg, Locale.US);
            OperationResultType operationResultType = ((OperationResult) object).createOperationResultType(resolveKeys);
            result = serializer.serializeAnyData(operationResultType, fakeQName);
        } else {
            result = serializer.serializeAnyData(object, fakeQName);
        }

        return result;
    }

    public <T> T parse(String text, Class<T> type) throws SchemaException, IOException {
        return parse(new ByteArrayInputStream(text.getBytes()), type);
    }

    public <T> T parse(InputStream is, Class<T> type) throws SchemaException, IOException {
        PrismParser parser = getParser(is);

        T object;
        if (PrismObject.class.isAssignableFrom(type)) {
            object = (T) parser.parse();
        } else {
            object = parser.parseRealValue(type);
        }

        return object;
    }

    public PrismParser getParser(InputStream entityStream) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(entityStream).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public PrismParser getParser(String entity) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(entity).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public void validateResponse(Response response) throws AuthenticationException {
        int code = response.code();
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.fromStatusCode(response.code());
        String reason = status != null ? status.getReasonPhrase() : null;

        if (javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode() == response.code()) {
            throw new AuthenticationException(reason);
        }

        if (!response.isSuccessful()) {
            OperationResult result = getOperationResultFromResponse(response);
            throw new ClientException("Unknown response status: " + code + ", reason: " + reason, result);
        }
    }

    public OperationResult getOperationResultFromResponse(Response response) {
        OperationResult result = null;
        try {
            ResponseBody body = response.body();
            if (body != null) {
                PrismParser parser = getParser(body.string());

                OperationResultType resultType = parser.parseRealValue(OperationResultType.class);
                return resultType != null ? OperationResult.createOperationResult(resultType) : null;
            }
        } catch (Exception ex) {
        }

        return null;
    }
}
