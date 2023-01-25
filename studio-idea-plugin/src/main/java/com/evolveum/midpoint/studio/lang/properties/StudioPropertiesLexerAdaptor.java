package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class StudioPropertiesLexerAdaptor extends ANTLRLexerAdaptor {

    private StudioPropertiesLexerAdaptor(StudioPropertiesLexer lexer) {
        super(StudioPropertiesLanguage.INSTANCE, lexer);
    }

    public static StudioPropertiesLexerAdaptor newInstance() {
        StudioPropertiesLexer lexer = new StudioPropertiesLexer(null);

        return new StudioPropertiesLexerAdaptor(lexer);
    }
}
