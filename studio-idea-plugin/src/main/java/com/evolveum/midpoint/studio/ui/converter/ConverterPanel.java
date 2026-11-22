package com.evolveum.midpoint.studio.ui.converter;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.util.LanguageUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.OnePixelSplitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;
import java.awt.*;


/**
 * Created by Dominik.
 */
public class ConverterPanel extends SimpleToolWindowPanel {

    private static final Logger LOG = Logger.getInstance(ConverterPanel.class);

    public static final String NOTIFICATION_KEY = "midpointConverter";

    private final Project project;

    public ConverterPanel(Project project) {
        super(false);
        this.project = project;
        initLayout();
    }

    private void initLayout() {
        SmartEditorComponent leftEditor = new SmartEditorComponent(project, PlainTextLanguage.INSTANCE, "");
        SmartEditorComponent rightEditor = new SmartEditorComponent(project, PlainTextLanguage.INSTANCE, "");
        LanguageSelectorPanel targetLangSelector = new LanguageSelectorPanel(
                "Target language",
                new String[]{
                    XMLLanguage.INSTANCE.getID(),
                    JsonLanguage.INSTANCE.getID(),
                    YAMLLanguage.INSTANCE.getID()
                }
        );

        targetLangSelector.onLanguageChange(e ->
                rightEditor.switchLanguage(
                        LanguageUtils.findLanguageByID(targetLangSelector.getSelectedLanguage()),
                        rightEditor.getText()
                )
        );

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
        banner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton button = new JButton("Convert");
        button.setPreferredSize(new Dimension(150, 30));
        button.addActionListener(e -> {
            Language targetLanguage = LanguageUtils.findLanguageByID(targetLangSelector.getSelectedLanguage());
            String converted = convert(leftEditor.getText(), targetLanguage);
            if (converted != null && targetLanguage != null) {
                rightEditor.switchLanguage(targetLanguage, converted);
            }
        });
        banner.add(button, BorderLayout.CENTER);
        add(banner, BorderLayout.SOUTH);

        setContent(split);
    }

    /**
     * Returns converted text, or null when conversion failed and the user was notified.
     */
    @Nullable
    private String convert(@NotNull String code, @Nullable Language targetLanguage) {
        if (code.isBlank()) {
            return "";
        }

        String sourceLang = LanguageUtils.toPrismLanguage(LanguageUtils.detectLanguage(code));
        if (sourceLang == null) {
            notifyError("Input is not recognized as XML, JSON or YAML.");
            return null;
        }

        String targetLang = LanguageUtils.toPrismLanguage(targetLanguage);
        if (targetLang == null) {
            notifyError("Unsupported target language: " + targetLanguage);
            return null;
        }

        try {
            PrismContext prismCtx = StudioPrismContextService.getPrismContext(project);
            ParsingContext parsingCtx = prismCtx.createParsingContextForCompatibilityMode();

            RootXNode root = prismCtx.parserFor(code)
                    .language(sourceLang)
                    .context(parsingCtx)
                    .parseToXNode();

            return prismCtx.serializerFor(targetLang).serialize(root);
        } catch (Exception ex) {
            LOG.warn("Couldn't convert " + sourceLang + " to " + targetLang, ex);
            notifyError("Couldn't convert input: " + ex.getMessage());
            return null;
        }
    }

    private void notifyError(String message) {
        MidPointUtils.publishNotification(project, NOTIFICATION_KEY, "MidPoint converter", message, NotificationType.ERROR);
    }
}
