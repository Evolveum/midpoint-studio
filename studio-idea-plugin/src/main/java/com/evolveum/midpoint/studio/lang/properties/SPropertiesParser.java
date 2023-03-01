package com.evolveum.midpoint.studio.lang.properties;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesParser extends ANTLRParserAdaptor {

    public SPropertiesParser() {
        super(SPropertiesLanguage.INSTANCE, new com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser qp = (com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser) parser;

        if (root instanceof IFileElementType) {
            return qp.root();
        }

        throw new UnsupportedOperationException(String.format("cannot start parsing using root element %s", root));
    }
}
