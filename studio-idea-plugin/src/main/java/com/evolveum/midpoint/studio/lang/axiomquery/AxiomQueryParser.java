package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2;
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
        super(AxiomQueryLanguage.INSTANCE, new AxiomQueryParserV2(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        AxiomQueryParserV2 qp = (AxiomQueryParserV2) parser;

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
