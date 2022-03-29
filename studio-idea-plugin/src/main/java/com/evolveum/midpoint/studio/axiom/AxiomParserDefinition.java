package com.evolveum.midpoint.studio.axiom;

import com.evolveum.axiom.lang.antlr.AxiomLexer;
import com.evolveum.axiom.lang.antlr.AxiomParser;
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
public class AxiomParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(AxiomLanguage.INSTANCE);

    public AxiomParserDefinition() {
        PSIElementTypeFactory.defineLanguageIElementTypes(
                AxiomLanguage.INSTANCE,
                AxiomLexer.tokenNames,
                AxiomParser.ruleNames
        );
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        AxiomLexer lexer = new AxiomLexer(null);
        return new AxiomLexerAdaptor(lexer);
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new ANTLRv4GrammarParser();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return ANTLRv4TokenTypes.WHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return ANTLRv4TokenTypes.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new ANTLRv4FileRoot(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    /**
     * Convert from internal parse node (AST they call it) to final PSI node. This
     * converts only internal rule nodes apparently, not leaf nodes. Leaves
     * are just tokens I guess.
     */
    @NotNull
    public PsiElement createElement(ASTNode node) {
        return ANTLRv4ASTFactory.createInternalParseTreeNode(node);
    }
}
