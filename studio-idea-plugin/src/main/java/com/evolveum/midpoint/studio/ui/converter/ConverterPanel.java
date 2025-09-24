package com.evolveum.midpoint.studio.ui.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;

import javax.swing.*;
import java.awt.*;


/**
 * Created by Dominik.
 */
public class ConverterPanel extends SimpleToolWindowPanel {

    Project project;

    public ConverterPanel(Project project) {
        super(false);
        this.project = project;
        initLayout();
    }

    private void initLayout() {
        LanguageSelectorPanel leftSelector = new LanguageSelectorPanel("Language");
        EditorPanel leftEditor = new EditorPanel(project, "", "xml");

        LanguageSelectorPanel rightSelector = new LanguageSelectorPanel("Target language");
        EditorPanel rightEditor = new EditorPanel(project, "", "xml");

        // connect selector -> editor
        leftSelector.onLanguageChange(e -> {
            String lang = leftSelector.getSelectedLanguage();
            String currentText = leftEditor.getText();
            leftEditor.setEditor(currentText, lang);
        });

        rightSelector.onLanguageChange(e -> {
            String lang = rightSelector.getSelectedLanguage();
            String currentText = rightEditor.getText();
            rightEditor.setEditor(currentText, lang);
        });

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(leftSelector, BorderLayout.NORTH);
        leftContainer.add(leftEditor, BorderLayout.CENTER);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(rightSelector, BorderLayout.NORTH);
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
                    leftEditor.getText(),
                    leftSelector.getSelectedLanguage(),
                    rightSelector.getSelectedLanguage()
            );

            rightEditor.setEditor(convertedCode, rightSelector.getSelectedLanguage());
        });
        banner.add(button, BorderLayout.CENTER);
        add(banner, BorderLayout.SOUTH);

        setContent(split);
    }

    private String convert(Project project, String code, String codeLang, String targetLang) {
        try {
            PrismContext prismContext = StudioPrismContextService.getPrismContext(project);
            ParsingContext parsingCtx = prismContext.createParsingContextForCompatibilityMode();

            if (code.isEmpty()) {
                throw new Exception("Body input is empty.");
            }

            RootXNodeImpl root = (RootXNodeImpl) prismContext.parserFor(code)
                    .language(codeLang)
                    .context(parsingCtx)
                    .parseToXNode();

            return prismContext.serializerFor(targetLang).serialize(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
