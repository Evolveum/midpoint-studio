/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.impl.lang.prism;

import com.evolveum.concepts.Argument;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.Definition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Quick fix in editor for MidPoint XML JSON YAML objects with use PRISM framework
 */
public class PrismIntentionAction implements IntentionAction {

    @SafeFieldForPreview
    private final ValidationLog validationLog;

    @SafeFieldForPreview
    private PsiElement rootElement = null;

    public PrismIntentionAction(ValidationLog validationLog) {
        this.validationLog = validationLog;
    }

    @Override
    public @NotNull String getText() {
        if (validationLog.specification().equals(ValidationLogType.Specification.MISSING_DEFINITION)) {
            return "Fix definition type";
        }

        return "";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Quick fix with use PRISM framework";
    }

    @Override
    public boolean isAvailable(
            @NotNull Project project,
            Editor editor,
            PsiFile file) {
        var elementAtLineColumn = PsiUtils.getElementAtLineColumn(file, validationLog.location().getLine(), validationLog.location().getChar());
        var language = file.getLanguage();
        PsiElement positionElement = null;

        if (language.isKindOf(XMLLanguage.INSTANCE)) {
            positionElement = PsiUtils.findXmlTagParent(elementAtLineColumn);
            if (positionElement.getParent() instanceof XmlDocument) {
                rootElement = positionElement;
            }
        } else if (language.isKindOf(JsonLanguage.INSTANCE)) {
            positionElement = PsiUtils.findJsonParent(elementAtLineColumn);

            if ((positionElement != null && positionElement.getParent() instanceof JsonFile) ||
                    ((positionElement != null && positionElement.getParent() != null) && positionElement.getParent().getParent() instanceof JsonFile)) {
                rootElement = positionElement;
            }
        } else if (language.isKindOf(YAMLLanguage.INSTANCE)) {
            positionElement = PsiUtils.findYamlKeyValueParent(elementAtLineColumn);

            if ((positionElement != null && positionElement.getParent() != null && positionElement.getParent().getParent() != null) &&
                    positionElement.getParent().getParent().getParent() instanceof YAMLDocument) {
                rootElement = positionElement;
            }
        }

        return file.isValid() && rootElement != null && !validationLog.specification().equals(ValidationLogType.Specification.UNKNOW);
    }

    @Override
    public void invoke(
            @NotNull Project project,
            Editor editor,
            PsiFile file) {
        if (validationLog.specification().equals(ValidationLogType.Specification.MISSING_DEFINITION)) {
            fixTypeDefinition(project, editor, file);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private void fixTypeDefinition(Project project, Editor editor, PsiFile file) {
        var arguments = validationLog.technicalMessage().arguments();
        List<Definition> definitions = Arrays.stream(arguments)
                        .filter(arg -> arg.type() == Argument.ArgumentType.DEFINITION_LIST)
                        .flatMap(arg -> ((List<?>) arg.value()).stream())
                        .filter(Definition.class::isInstance)
                        .map(Definition.class::cast)
                        .toList();

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(definitions)
                .setTitle("Choose Definition Type")
                .setRenderer(new ListCellRenderer<>() {
                    private final JLabel label = new JLabel();
                    @Override
                    public Component getListCellRendererComponent(JList<? extends Definition> list, Definition value, int index,
                                                                  boolean isSelected, boolean cellHasFocus) {
                        label.setText(value.getTypeName().getLocalPart());
                        label.setOpaque(true);
                        label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                        return label;
                    }
                })
                .setItemChosenCallback(choice -> {
                    if (file.getFileType().getName().equalsIgnoreCase(PrismContext.LANG_XML)) {
                        if (rootElement instanceof XmlTag xmlTag) {
                            XmlAttribute attr = xmlTag.getAttribute("xsi:type");

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                if (attr != null) {
                                    attr.setValue(choice.getTypeName().getLocalPart());
                                } else {
                                    xmlTag.setAttribute("xsi:type", choice.getTypeName().getLocalPart());
                                }
                            });
                        }
                    } else if (file.getFileType().getName().equalsIgnoreCase(PrismContext.LANG_JSON)) {
                        if (rootElement instanceof JsonProperty jsonProperty) {
                            if (jsonProperty.getValue() instanceof JsonObject jsonObject) {
                                JsonProperty prop = jsonObject.findProperty("@type");

                                WriteCommandAction.runWriteCommandAction(project, () -> {
                                    JsonElementGenerator generator = new JsonElementGenerator(project);
                                    if (prop != null) {
                                        Objects.requireNonNull(prop.getValue()).replace(generator.createValue("\"" +
                                                choice.getTypeName().getLocalPart() + "\""));
                                    } else {
                                        PsiElement firstProperty = jsonObject.getFirstChild();
                                        jsonObject.addAfter(generator.createComma(), firstProperty);
                                        jsonObject.addAfter(generator.createProperty("@type", "\"" +
                                                choice.getTypeName().getLocalPart() + "\""), firstProperty);
                                    }
                                });
                            }
                        }
                    } else if (file.getFileType().getName().equalsIgnoreCase(PrismContext.LANG_YAML)) {
                        if (rootElement instanceof YAMLBlockMappingImpl yamlBlockMapping) {
                            if (yamlBlockMapping.getText() != null) {
                                YAMLElementGenerator generator = YAMLElementGenerator.getInstance(project);
                                YAMLMapping mapping = PsiTreeUtil.findChildOfType(generator.createDummyYamlWithText(
                                        "!<" + choice.getTypeName().getLocalPart() + ">\n   " +
                                                (
                                                    yamlBlockMapping.getTag() != null
                                                        ? yamlBlockMapping.getText().replace(Objects.requireNonNull(yamlBlockMapping.getTag()).getText(), "")
                                                        : yamlBlockMapping.getText()
                                                )
                                ), YAMLMapping.class);

                                if (mapping != null) {
                                    WriteCommandAction.runWriteCommandAction(project, () -> {
                                        yamlBlockMapping.replace(mapping);
                                    });
                                }
                            }
                        }
                    }
                })
                .createPopup()
                .showInBestPositionFor(editor);
    }
}
