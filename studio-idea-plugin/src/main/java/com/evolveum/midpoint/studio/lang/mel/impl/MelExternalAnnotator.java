package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MelExternalAnnotator
        extends ExternalAnnotator<MelExternalAnnotator.Info, List<ValidationMessage>> {

    public record Info(String text, VirtualFile file) {
    }

    @Override
    public Info collectInformation(@NotNull PsiFile file) {
        return new Info(file.getText(), file.getVirtualFile());
    }

    @Override
    public List<ValidationMessage> doAnnotate(Info info) {
        ANTLRInputStream input = new ANTLRInputStream(info.text());
        MELLexer lexer = new MELLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MELParser parser = new MELParser(tokens);
        parser.removeErrorListeners();
        lexer.removeErrorListeners();

        MELParser.StartContext tree = parser.start();
        MelExtensionValidator validator = new MelExtensionValidator();
        return validator.analyze(tree);
    }

    @Override
    public void apply(@NotNull PsiFile file,
                      List<ValidationMessage> errors,
                      @NotNull AnnotationHolder holder) {

        for (var err : errors) {
            HighlightSeverity severity = switch (err.severity()) {
                case ERROR -> HighlightSeverity.ERROR;
                case WARNING -> HighlightSeverity.WARNING;
                case INFO -> HighlightSeverity.INFORMATION;
            };

            TextRange range = tokenToTextRange(err.token());
            holder.newAnnotation(severity, err.message())
                    .range(range)
                    .create();
        }
    }

    /**
     * Convert an ANTLR Token to an IntelliJ TextRange within the file.
     */
    private TextRange tokenToTextRange(Token token) {
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1; // IntelliJ end is exclusive
        return new TextRange(start, stop);
    }
}