package com.evolveum.midpoint.studio.lang.axiomquery;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.intellij.lang.annotations.MagicConstant;

import java.util.List;

import static com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryLexerV2.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryTokenTypes {

    public static IElementType BAD_TOKEN_TYPE = new IElementType("BAD_TOKEN", AxiomQueryLanguage.INSTANCE);

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES = PSIElementTypeFactory.getTokenIElementTypes(AxiomQueryLanguage.INSTANCE);

    public static final List<RuleIElementType> RULE_ELEMENT_TYPES = PSIElementTypeFactory.getRuleIElementTypes(AxiomQueryLanguage.INSTANCE);

    public static final TokenSet COMMENTS = PSIElementTypeFactory.createTokenSet(
            AxiomQueryLanguage.INSTANCE,
            LINE_COMMENT);

    public static final TokenSet WHITESPACES = PSIElementTypeFactory.createTokenSet(
            AxiomQueryLanguage.INSTANCE,
            SEP);

    public static final TokenSet STRINGS =
            PSIElementTypeFactory.createTokenSet(
                    AxiomQueryLanguage.INSTANCE,
                    STRING_BACKTICK,
                    STRING_SINGLEQUOTE,
                    STRING_DOUBLEQUOTE,
                    STRING_MULTILINE,
                    STRING_BACKTICK_TRIQOUTE);

    public static final TokenSet KEYWORDS = PSIElementTypeFactory.createTokenSet(
            AxiomQueryLanguage.INSTANCE,
            AND_KEYWORD,
            OR_KEYWORD,
            NOT_KEYWORD);

    public static RuleIElementType getRuleElementType(@MagicConstant(valuesFromClass = com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2.class) int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(@MagicConstant(valuesFromClass = com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryLexerV2.class) int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
