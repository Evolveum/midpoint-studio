package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomParser extends ANTLRParserAdaptor {

    public AxiomParser() {
        super(AxiomLanguage.INSTANCE, new com.evolveum.axiom.lang.antlr.AxiomParser(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        // todo implement
        return null;
    }
}
