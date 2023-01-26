package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.studio.lang.axiomquery.psi.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryASTFactory extends ASTFactory {

    public static PsiElement createInternalParseTreeNode(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }

    @Override
    public @Nullable CompositeElement createComposite(@NotNull IElementType type) {
        if (type instanceof IFileElementType) {
            return new FileElement(type, null);
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_root)) {
            return new AQRoot();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_path)) {
            return new AQPath();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_filter)) {
            return new AQFilter();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_itemFilter)) {
            return new AQItemFilter();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_subfilterOrValue)) {
            return new AQSubfilterOrValue();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_filterName)) {
            return new AQFilterName();
        }

        return new CompositeElement(type);
    }

    @Override
    public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == AxiomQueryTokenTypes.getRuleElementType(RULE_filterNameAlias)) {
            return new AQFilterNameAlias(text);
        }

        return new LeafPsiElement(type, text);
    }
}
