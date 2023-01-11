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
        PSIElementTypeFactory.defineLanguageIElementTypes(AxiomLanguage.INSTANCE, AxiomParser.tokenNames, AxiomParser.ruleNames);
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        AxiomLexer lexer = new AxiomLexer(null);
        return new AxiomLexerAdaptor(lexer);
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new com.evolveum.midpoint.studio.axiom.AxiomParser();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return AxiomTokenTypes.WHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return AxiomTokenTypes.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return AxiomTokenTypes.STRINGS;
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new AxiomFileRoot(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return AxiomASTFactory.createInternalParseTreeNode(node);
    }
}
