package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.common.rest.MidpointXmlProvider;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompatibilityXmlProvider<T> extends MidpointXmlProvider<T> {

    private PrismContext prismContext;

    public CompatibilityXmlProvider(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    protected PrismSerializer<String> getSerializer() {
        return prismContext.xmlSerializer();
    }

    @Override
    protected PrismParser getParser(InputStream entityStream) {
        ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
        return prismContext.parserFor(entityStream).language(PrismContext.LANG_XML).context(parsingContext);
    }
}
