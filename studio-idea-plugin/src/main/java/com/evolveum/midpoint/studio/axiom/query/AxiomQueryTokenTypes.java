package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.intellij.lang.annotations.MagicConstant;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryTokenTypes {

    public static IElementType BAD_TOKEN_TYPE = new IElementType("BAD_TOKEN", AxiomQueryLanguage.INSTANCE);

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES = PSIElementTypeFactory.getTokenIElementTypes(AxiomQueryLanguage.INSTANCE);

    public static final List<RuleIElementType> RULE_ELEMENT_TYPES = PSIElementTypeFactory.getRuleIElementTypes(AxiomQueryLanguage.INSTANCE);

    public static final TokenSet COMMENTS = PSIElementTypeFactory.createTokenSet(
            AxiomQueryLanguage.INSTANCE,
            AxiomQueryLexer.LINE_COMMENT);

    public static final TokenSet WHITESPACES = PSIElementTypeFactory.createTokenSet(
            AxiomQueryLanguage.INSTANCE,
            AxiomQueryLexer.SEP);

    public static final TokenSet STRINGS =
            PSIElementTypeFactory.createTokenSet(
                    AxiomQueryLanguage.INSTANCE,
                    AxiomQueryLexer.STRING_BACKTICK,
                    AxiomQueryLexer.STRING_SINGLEQUOTE,
                    AxiomQueryLexer.STRING_DOUBLEQUOTE,
                    AxiomQueryLexer.STRING_MULTILINE,
                    AxiomQueryLexer.STRING_BACKTICK_TRIQOUTE);

    public static final TokenSet KEYWORDS = TokenSet.EMPTY;

    public static RuleIElementType getRuleElementType(@MagicConstant(valuesFromClass = AxiomQueryParser.class) int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(@MagicConstant(valuesFromClass = AxiomQueryLexer.class) int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
