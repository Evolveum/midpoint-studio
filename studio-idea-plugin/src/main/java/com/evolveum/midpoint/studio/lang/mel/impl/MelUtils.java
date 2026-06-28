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

    /**
     * Returns the trailing identifier (letters, digits, underscore) of {@code text},
     * e.g. for "focus.format" returns "format". Used to find the receiver of a
     * member/namespace call by walking backwards from the call site, without
     * requiring a typed PSI tree.
     */
    public static String extractLastIdentifier(String text) {
        int end = text.length();
        int start = end;
        while (start > 0) {
            char c = text.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_') {
                start--;
            } else {
                break;
            }
        }
        return text.substring(start, end);
    }
}
