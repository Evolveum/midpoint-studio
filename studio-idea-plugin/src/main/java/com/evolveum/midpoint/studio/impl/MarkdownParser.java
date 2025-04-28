package com.evolveum.midpoint.studio.impl;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Paragraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class MarkdownParser {

    public record ParsedMarkdown(String code, String language, String text) {}

    public static List<ParsedMarkdown> extractBlocks(String markdownText) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownText);
        List<ParsedMarkdown> blocks = new ArrayList<>();

        for (Node node : document.getChildren()) {
            if (node instanceof FencedCodeBlock codeBlock) {
                String language = codeBlock.getInfo().toString();
                String code = codeBlock.getContentChars().toString();
                blocks.add(new ParsedMarkdown(code, language, null));
            } else if (node instanceof Paragraph paragraph) {
                String text = paragraph.getChars().toString();
                blocks.add(new ParsedMarkdown(null, null, text));
            }
        }

        return blocks;
    }
}
