package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelCommenter implements CodeDocumentationAwareCommenter {

    @Override
    public @Nullable String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public @Nullable IElementType getLineCommentTokenType() {
        return MelTokenTypes.TOKEN_ELEMENT_TYPES.get(MELLexer.COMMENT);
    }

    @Override
    public @Nullable IElementType getBlockCommentTokenType() {
        return null;
    }

    @Override
    public @Nullable IElementType getDocumentationCommentTokenType() {
        return null;
    }

    @Override
    public @Nullable String getDocumentationCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getDocumentationCommentLinePrefix() {
        return null;
    }

    @Override
    public @Nullable String getDocumentationCommentSuffix() {
        return null;
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        return false;
    }

    @Override
    public @Nullable String getBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getBlockCommentSuffix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }
}
