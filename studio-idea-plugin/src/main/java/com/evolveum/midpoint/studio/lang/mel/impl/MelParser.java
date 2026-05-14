package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser;
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
public class MelParser extends ANTLRParserAdaptor {

    public MelParser() {
        super(MelLanguage.INSTANCE, new MELParser(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        MELParser qp = (MELParser) parser;

        if (root instanceof IFileElementType) {
            return qp.start();
        }

        throw new UnsupportedOperationException(String.format("cannot start parsing using root element %s", root));
    }

    @Override
    protected ANTLRParseTreeToPSIConverter createListener(Parser parser, IElementType root, PsiBuilder builder) {
        return super.createListener(parser, root, builder);
    }
}
