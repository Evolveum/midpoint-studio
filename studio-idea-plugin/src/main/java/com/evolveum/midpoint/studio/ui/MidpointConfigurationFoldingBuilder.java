package com.evolveum.midpoint.studio.ui;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.ArrayList;
import java.util.List;

public class MidpointConfigurationFoldingBuilder extends FoldingBuilderEx {

    private final List<String> CONCEPTS =  List.of("_metadata", "metadata", "cachingMetadata");

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {

        if (b) return FoldingDescriptor.EMPTY;

        List<FoldingDescriptor> descriptors = new ArrayList<>();

        psiElement.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof XmlTag xmlTag) {
                    handleXml(xmlTag, descriptors);
                }

                if (element instanceof JsonProperty jsonObject) {
                    handleJson(jsonObject, descriptors);
                }

                if (element instanceof YAMLKeyValue yamlMapping) {
                    handleYaml(yamlMapping, descriptors);
                }

                super.visitElement(element);
            }
        });

        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode astNode) {
        PsiElement psiElement = astNode.getPsi();

        if (psiElement instanceof PsiNamedElement) {
            return ((PsiNamedElement) psiElement).getName();
        }

        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }

    private void handleXml(XmlTag tag, List<FoldingDescriptor> descriptors) {
        if (CONCEPTS.contains(tag.getLocalName())) {
            descriptors.add(new FoldingDescriptor(
                tag.getNode(),
                tag.getTextRange()
            ));
        }
    }

    private void handleJson(JsonProperty property, List<FoldingDescriptor> descriptors) {
        if (CONCEPTS.contains(property.getName())) {
            JsonValue value = property.getValue();
            if (value != null) {
                descriptors.add(new FoldingDescriptor(
                        value.getNode(),
                        value.getTextRange()
                ));
            }
        }
    }

    private void handleYaml(YAMLKeyValue keyValue, List<FoldingDescriptor> descriptors) {
        if (CONCEPTS.contains(keyValue.getKeyText())) {
            YAMLValue value = keyValue.getValue();
            if (value != null) {
                descriptors.add(new FoldingDescriptor(
                        value.getNode(),
                        value.getTextRange()
                ));
            }
        }
    }
}
