package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.AxiomParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.axiom.AxiomTokenTypes;
import com.evolveum.midpoint.studio.axiom.PsiElementFactory;
import com.evolveum.midpoint.studio.axiom.query.psi.*;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryASTFactory extends ASTFactory {

    private static final Map<IElementType, PsiElementFactory> RULE_ELEMENT_TYPE_TO_PSI_FACTORY = new HashMap<>();

    static {
        // later auto gen with tokens from some spec in grammar?

        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(AxiomParser.RULE_path), PathNode.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_parserRuleSpec), ParserRuleSpecNode.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_lexerRule), LexerRuleSpecNode.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_grammarSpec), GrammarSpecNode.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_modeSpec), ModeSpecNode.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_action), AtAction.Factory.INSTANCE);
//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_identifier), TokenSpecNode.Factory.INSTANCE);
    }

    public static PsiElement createInternalParseTreeNode(ASTNode node) {
        IElementType tokenType = node.getElementType();
        PsiElementFactory factory = RULE_ELEMENT_TYPE_TO_PSI_FACTORY.get(tokenType);

        return factory != null ? factory.createElement(node) : new ASTWrapperPsiElement(node);
    }

    @Override
    public @Nullable CompositeElement createComposite(@NotNull IElementType type) {
        if (type instanceof IFileElementType) {
            return new FileElement(type, null);
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_root)) {
            return new AQRoot();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_path)) {
            return new AQPath();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filter)) {
            return new AQFilter();
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_itemFilter)) {
            return new AQItemFilter();
        }

        return new CompositeElement(type);
    }

    @Override
    public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterName)) {
            return new AQFilterName(text);
        }

        if (type == AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterName)) {
            return new AQFilterAlias(text);
        }

        return new LeafPsiElement(type, text);
    }
}
