package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class SPropertiesLexerAdaptor extends ANTLRLexerAdaptor {

    private SPropertiesLexerAdaptor(StudioPropertiesLexer lexer) {
        super(SPropertiesLanguage.INSTANCE, lexer);
    }

    public static SPropertiesLexerAdaptor newInstance() {
        StudioPropertiesLexer lexer = new StudioPropertiesLexer(null);

        return new SPropertiesLexerAdaptor(lexer);
    }
}
