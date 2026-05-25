package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.intellij.lang.annotations.MagicConstant;

import java.util.List;

import static com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelTokenTypes {

    static {
        MelUtils.initialize();
    }

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES =
            PSIElementTypeFactory.getTokenIElementTypes(MelLanguage.INSTANCE);

    public static final List<RuleIElementType> RULE_ELEMENT_TYPES =
            PSIElementTypeFactory.getRuleIElementTypes(MelLanguage.INSTANCE);

    public static final TokenSet COMMENTS =
            PSIElementTypeFactory.createTokenSet(MelLanguage.INSTANCE, COMMENT);

    public static final TokenSet WHITESPACES =
            PSIElementTypeFactory.createTokenSet(MelLanguage.INSTANCE, WHITESPACE);

    public static final TokenSet STRINGS =
            PSIElementTypeFactory.createTokenSet(MelLanguage.INSTANCE, STRING);

    public static final TokenSet KEYWORDS =
            PSIElementTypeFactory.createTokenSet(MelLanguage.INSTANCE, LOGICAL_AND, LOGICAL_OR);

    public static final TokenIElementType BAD_TOKEN_TYPE =
            TOKEN_ELEMENT_TYPES.get(INVALID_CHAR);

    public static RuleIElementType getRuleElementType(
            @MagicConstant(valuesFromClass = MelParser.class) int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(
            @MagicConstant(valuesFromClass = MelParser.class) int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
