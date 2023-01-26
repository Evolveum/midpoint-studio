package com.evolveum.midpoint.studio.lang.axiom;

import com.evolveum.axiom.lang.antlr.AxiomLexer;
import com.evolveum.axiom.lang.antlr.AxiomParser;
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
public class AxiomTokenTypes {

    public static IElementType BAD_TOKEN_TYPE = new IElementType("BAD_TOKEN", AxiomLanguage.INSTANCE);

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES = PSIElementTypeFactory.getTokenIElementTypes(AxiomLanguage.INSTANCE);

    public static final List<RuleIElementType> RULE_ELEMENT_TYPES = PSIElementTypeFactory.getRuleIElementTypes(AxiomLanguage.INSTANCE);

    public static final TokenSet COMMENTS = PSIElementTypeFactory.createTokenSet(
            AxiomLanguage.INSTANCE,
            AxiomLexer.LINE_COMMENT);

    public static final TokenSet WHITESPACES = TokenSet.EMPTY;

    public static final TokenSet STRINGS =
            PSIElementTypeFactory.createTokenSet(
                    AxiomLanguage.INSTANCE,
                    AxiomLexer.STRING_SINGLEQUOTE,
                    AxiomLexer.STRING_DOUBLEQUOTE,
                    AxiomLexer.STRING_MULTILINE_START);

    public static final TokenSet KEYWORDS = TokenSet.EMPTY;

    public static RuleIElementType getRuleElementType(@MagicConstant(valuesFromClass = AxiomParser.class) int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(@MagicConstant(valuesFromClass = AxiomLexer.class) int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
