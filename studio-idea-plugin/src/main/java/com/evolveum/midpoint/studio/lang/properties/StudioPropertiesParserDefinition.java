package com.evolveum.midpoint.studio.lang.properties;

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
public class StudioPropertiesParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(StudioPropertiesLanguage.INSTANCE);

    public StudioPropertiesParserDefinition() {
        PSIElementTypeFactory.defineLanguageIElementTypes(StudioPropertiesLanguage.INSTANCE,
                com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser.tokenNames,
                com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser.ruleNames);
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return StudioPropertiesLexerAdaptor.newInstance();
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new StudioPropertiesParser();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return StudioPropertiesTokenTypes.WHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return StudioPropertiesTokenTypes.STRINGS;
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new StudioPropertiesFileRoot(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return StudioPropertiesASTFactory.createInternalParseTreeNode(node);
    }
}
