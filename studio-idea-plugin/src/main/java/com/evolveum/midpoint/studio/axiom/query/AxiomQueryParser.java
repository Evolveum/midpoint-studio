package com.evolveum.midpoint.studio.axiom.query;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryParser extends ANTLRParserAdaptor {

    public AxiomQueryParser() {
        super(AxiomQueryLanguage.INSTANCE, new com.evolveum.axiom.lang.antlr.query.AxiomQueryParser(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        // todo implement
        return null;
    }
}
