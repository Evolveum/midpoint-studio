package com.evolveum.midpoint.studio.ui.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.editor.EditorPanel;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;
import java.awt.*;


/**
 * Created by Dominik.
 */
public class ConverterPanel extends SimpleToolWindowPanel {

    private final Project project;
    private Language codeLang;

    public ConverterPanel(Project project) {
        super(false);
        this.project = project;
        initLayout();
    }

    private void initLayout() {
        EditorPanel leftEditor = new EditorPanel(project);
        EditorPanel rightEditor = new EditorPanel(project);
        LanguageSelectorPanel targetLangSelector = new LanguageSelectorPanel("Target language");

        leftEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                codeLang = detectContentLang(leftEditor.getDocument().getText());
                leftEditor.updateHighlighter(codeLang);
            }
        });

        targetLangSelector.onLanguageChange(e -> {
            rightEditor.updateHighlighter(findLanguageByID(targetLangSelector.getSelectedLanguage()));
        });

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(leftEditor, BorderLayout.CENTER);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(targetLangSelector, BorderLayout.NORTH);
        rightContainer.add(rightEditor, BorderLayout.CENTER);

        OnePixelSplitter split = new OnePixelSplitter(false);
        split.setProportion(0.5f);
        split.setFirstComponent(leftContainer);
        split.setSecondComponent(rightContainer);

        // Convert button
        JPanel banner = new JPanel();
        banner.setBackground(JBColor.LIGHT_GRAY);
        banner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton button = new JButton("Convert");
        button.setPreferredSize(new Dimension(150, 30));
        button.addActionListener(e -> {
            String convertedCode = convert(project,
                    leftEditor.getContent(),
                    targetLangSelector.getSelectedLanguage()
            );

            rightEditor.setContent(convertedCode);
            rightEditor.updateHighlighter(findLanguageByID(targetLangSelector.getSelectedLanguage()));
        });
        banner.add(button, BorderLayout.CENTER);
        add(banner, BorderLayout.SOUTH);

        setContent(split);
    }

    private String convert(Project project, String code, String targetLang) {
        try {
            PrismContext prismCtx = StudioPrismContextService.getPrismContext(project);
            ParsingContext parsingCtx = prismCtx.createParsingContextForCompatibilityMode();

            if (code.isEmpty()) {
                throw new Exception("Body input is empty.");
            }

            assert codeLang != null;

            RootXNodeImpl root = (RootXNodeImpl) prismCtx.parserFor(code)
                    .language(codeLang.getID().toLowerCase())
                    .context(parsingCtx)
                    .parseToXNode();

            return prismCtx.serializerFor(targetLang).serialize(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Language detectContentLang(String text) {
        if (text.trim().startsWith("<")) {
            return XMLLanguage.INSTANCE;
        }
        if (text.trim().startsWith("{") || text.trim().startsWith("[")) {
            return JsonLanguage.INSTANCE;
        }
        if (text.contains(":") && text.contains("\n")) {
            return YAMLLanguage.INSTANCE;
        }

        return null;
    }

    private Language findLanguageByID(String id) {
        return Language.getRegisteredLanguages().stream()
                .filter(l -> l.getID().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
}
