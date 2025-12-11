package com.evolveum.midpoint.studio.ui.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.util.LanguageUtils;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
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

    private final Project project;

    public ConverterPanel(Project project) {
        super(false);
        this.project = project;
        initLayout();
    }

    private void initLayout() {
        SmartEditorComponent leftEditor = new SmartEditorComponent(project, PlainTextLanguage.INSTANCE);
        SmartEditorComponent rightEditor = new SmartEditorComponent(project, PlainTextLanguage.INSTANCE);
        LanguageSelectorPanel targetLangSelector = new LanguageSelectorPanel("Target language");

        targetLangSelector.onLanguageChange(e -> {
            rightEditor.updateLanguage(LanguageUtils.findLanguageByID(targetLangSelector.getSelectedLanguage()));
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

        JPanel banner = new JPanel();
        banner.setBackground(JBColor.LIGHT_GRAY);
        banner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton button = new JButton("Convert");
        button.setPreferredSize(new Dimension(150, 30));
        button.addActionListener(e -> {
            rightEditor.updateLanguage(LanguageUtils.findLanguageByID(targetLangSelector.getSelectedLanguage()));
            rightEditor.setText(convert(project,
                    leftEditor.getText(),
                    targetLangSelector.getSelectedLanguage()
            ));
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

            RootXNodeImpl root = (RootXNodeImpl) prismCtx.parserFor(code)
                    .language(LanguageUtils.detectLanguage(code).getID().toLowerCase())
                    .context(parsingCtx)
                    .parseToXNode();

            return prismCtx.serializerFor(targetLang).serialize(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
