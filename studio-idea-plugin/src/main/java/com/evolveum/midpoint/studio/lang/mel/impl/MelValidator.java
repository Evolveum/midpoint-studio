package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

public class MelValidator {

    public static List<ValidationMessage> validate(String text) {
        ANTLRInputStream input = new ANTLRInputStream(text);
        MELLexer lexer = new MELLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MELParser parser = new MELParser(tokens);
        parser.removeErrorListeners();
        lexer.removeErrorListeners();

        MELParser.StartContext tree = parser.start();
        return new MelExtensionValidator().analyze(tree);
    }
}
