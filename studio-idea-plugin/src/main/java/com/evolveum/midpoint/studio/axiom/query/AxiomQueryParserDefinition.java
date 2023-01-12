package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(AxiomQueryLanguage.INSTANCE);

    public AxiomQueryParserDefinition() {
        PSIElementTypeFactory.defineLanguageIElementTypes(AxiomQueryLanguage.INSTANCE, AxiomQueryParser.tokenNames, AxiomQueryParser.ruleNames);
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return AxiomQueryLexerAdaptor.newInstance();
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new com.evolveum.midpoint.studio.axiom.query.AxiomQueryParser();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return AxiomQueryTokenTypes.WHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return AxiomQueryTokenTypes.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return AxiomQueryTokenTypes.STRINGS;
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new AxiomQueryFileRoot(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return AxiomQueryASTFactory.createInternalParseTreeNode(node);
    }
}
