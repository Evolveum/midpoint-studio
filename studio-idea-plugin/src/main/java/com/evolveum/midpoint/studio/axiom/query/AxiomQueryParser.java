package com.evolveum.midpoint.studio.axiom.query;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParseTreeToPSIConverter;
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
        com.evolveum.axiom.lang.antlr.query.AxiomQueryParser qp = (com.evolveum.axiom.lang.antlr.query.AxiomQueryParser) parser;

        if (root instanceof IFileElementType) {
            return qp.root();
        }

        throw new UnsupportedOperationException(String.format("cannot start parsing using root element %s", root));
    }

    @Override
    protected ANTLRParseTreeToPSIConverter createListener(Parser parser, IElementType root, PsiBuilder builder) {
        return super.createListener(parser, root, builder);
    }
}
