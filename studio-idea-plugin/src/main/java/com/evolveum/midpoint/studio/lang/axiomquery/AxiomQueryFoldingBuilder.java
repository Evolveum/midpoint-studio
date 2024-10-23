package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryFoldingBuilder extends CustomFoldingBuilder {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {

    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return getPlaceholderText(SourceTreeToPsiMap.treeElementToPsi(node));
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    private static String getPlaceholderText(PsiElement element) {
        if (element.getNode().getElementType() == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.COMMENT)) {
            return "//...";
        }

        return "...";
    }
}
