package com.evolveum.midpoint.studio.impl;

import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik.
 */
public class MarkdownParser {

    public record ParsedMarkdown(String code, String language, String html) {}

    public static List<ParsedMarkdown> extractBlocks(String markdownText) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(markdownText);
        List<ParsedMarkdown> blocks = new ArrayList<>();

        for (Node node : document.getChildren()) {
            if (node instanceof FencedCodeBlock codeBlock) {
                String language = codeBlock.getInfo().toString();
                String code = codeBlock.getContentChars().toString();
                blocks.add(new ParsedMarkdown(code, language, null));
            } else {
                String html = renderer.render(node);
                blocks.add(new ParsedMarkdown(null, null, html));
            }
        }

        return blocks;
    }
}
