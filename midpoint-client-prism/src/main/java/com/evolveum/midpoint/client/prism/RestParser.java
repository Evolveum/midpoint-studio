package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestParser {

    private PrismContext prismContext;

    private String language;

    public RestParser(PrismContext prismContext) {
        this(prismContext, PrismContext.LANG_XML);
    }

    public RestParser(PrismContext prismContext, String language) {
        this.prismContext = prismContext;
        this.language = language;
    }

    private PrismParser createParser(InputStream stream) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(stream).language(language).context(parsingContext);
    }

    private PrismSerializer<String> createSerializer() {
        return prismContext.xmlSerializer();
    }

    public <T> void write(T object, OutputStream stream) throws SchemaException, IOException {
        QName fakeQName = new QName(PrismConstants.NS_TYPES, "object");
        String serializedForm;

        PrismSerializer<String> serializer = createSerializer()
                .options(SerializationOptions.createSerializeReferenceNames());

        if (object instanceof PrismObject) {
            serializedForm = serializer.serialize((PrismObject<?>) object);
        } else if (object instanceof OperationResult) {
            OperationResultType operationResultType = ((OperationResult) object).createOperationResultType();
            serializedForm = serializer.serializeAnyData(operationResultType, fakeQName);
        } else {
            serializedForm = serializer.serializeAnyData(object, fakeQName);
        }
        stream.write(serializedForm.getBytes(StandardCharsets.UTF_8));
    }

    public <T> T read(Class<T> type, InputStream stream) throws SchemaException, IOException {
        if (stream == null) {
            return null;
        }

        PrismParser parser = createParser(stream);

        T object;

        if (ObjectType.class.isAssignableFrom(type)) {
            PrismObject obj = parser.parse();
            object = (T) obj.asObjectable();
        } else if (PrismObject.class.isAssignableFrom(type)) {
            object = (T) parser.parse();
        } else {
            object = parser.parseRealValue();
        }

        return object;
    }
}
