package com.evolveum.midpoint.studio.lang.mel.impl;

import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;

public class MelUtils {

    public static void initialize() {
        PSIElementTypeFactory.defineLanguageIElementTypes(
                MelLanguage.INSTANCE,
                com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer.tokenNames,
                com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.ruleNames
        );
    }
}
