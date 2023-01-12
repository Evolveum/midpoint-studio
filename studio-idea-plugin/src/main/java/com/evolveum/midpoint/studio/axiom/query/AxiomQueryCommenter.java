package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCommenter implements CodeDocumentationAwareCommenter {

    @Override
    public @Nullable String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public @Nullable IElementType getLineCommentTokenType() {
        return AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.LINE_COMMENT);
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
