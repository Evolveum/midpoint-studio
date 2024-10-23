package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomASTFactory extends ASTFactory {

    private static final Map<IElementType, PsiElementFactory> RULE_ELEMENT_TYPE_TO_PSI_FACTORY = new HashMap<>();

    static {
        // later auto gen with tokens from some spec in grammar?

//        RULE_ELEMENT_TYPE_TO_PSI_FACTORY.put(
//                AxiomTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.axiom.lang.antlr.AxiomParser.RULE_rules), RulesNode.Factory.INSTANCE);
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
}
