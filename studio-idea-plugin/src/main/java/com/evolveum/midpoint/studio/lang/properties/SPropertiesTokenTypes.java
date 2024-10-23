package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
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
public class SPropertiesTokenTypes {

    public static IElementType BAD_TOKEN_TYPE = new IElementType("BAD_TOKEN", SPropertiesLanguage.INSTANCE);

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES = PSIElementTypeFactory.getTokenIElementTypes(SPropertiesLanguage.INSTANCE);

    public static final List<RuleIElementType> RULE_ELEMENT_TYPES = PSIElementTypeFactory.getRuleIElementTypes(SPropertiesLanguage.INSTANCE);

    public static final TokenSet WHITESPACES = PSIElementTypeFactory.createTokenSet(
            SPropertiesLanguage.INSTANCE,
            StudioPropertiesLexer.SEPARATOR);

    public static final TokenSet STRINGS =
            PSIElementTypeFactory.createTokenSet(
                    SPropertiesLanguage.INSTANCE,
                    StudioPropertiesLexer.IDENTIFIER);

    public static final TokenSet KEYWORDS = PSIElementTypeFactory.createTokenSet(
            SPropertiesLanguage.INSTANCE,
            StudioPropertiesLexer.AT_SIGN,
            StudioPropertiesLexer.DOLLAR_SIGN);

    public static RuleIElementType getRuleElementType(@MagicConstant(valuesFromClass = SPropertiesParser.class) int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(@MagicConstant(valuesFromClass = StudioPropertiesLexer.class) int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
}
