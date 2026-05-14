package com.evolveum.midpoint.studio.lang.mel.impl;

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
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(MelLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return MelLexerAdaptor.newInstance();
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new MelParser();
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return MelTokenTypes.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return MelTokenTypes.WHITESPACES;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return MelTokenTypes.STRINGS;
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new MelFileRoot(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return MelASTFactory.createInternalParseTreeNode(node);
    }
}
