package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides Quick Documentation (Ctrl+Q / hover) for MEL extension functions
 * (e.g. "format.strftime", "log.info"), sourced from {@link MelExtensionRegistry}.
 *
 * MEL's PSI tree is a generic ANTLR rule tree without typed accessors, so the receiver
 * of a call is resolved the same way as in {@link MelCompletionContributor}: by looking
 * backwards at the raw file text rather than walking typed PSI/AST nodes.
 */
public class MelDocumentationProvider extends AbstractDocumentationProvider {

    private static final MelExtensionRegistry REGISTRY = MelExtensionValidator.REGISTRY;

    @Override
    public PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file,
                                                     @Nullable PsiElement contextElement, int targetOffset) {
        return contextElement;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        var fn = resolveFunction(element);
        if (fn == null) return null;

        var sb = new StringBuilder();
        sb.append(DocumentationMarkup.DEFINITION_START)
                .append(signature(fn))
                .append(DocumentationMarkup.DEFINITION_END);

        if (fn.documentation() != null && !fn.documentation().isBlank()) {
            sb.append(DocumentationMarkup.CONTENT_START)
                    .append(fn.documentation())
                    .append(DocumentationMarkup.CONTENT_END);
        }

        return sb.toString();
    }

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        var fn = resolveFunction(element);
        return fn == null ? null : signature(fn);
    }

    private String signature(MelExtensionRegistry.ExtensionFunction fn) {
        var sb = new StringBuilder(fn.name()).append("(");
        var params = fn.parameters();
        int start = fn.receiverTypes().isEmpty() ? 0 : 1;
        for (int i = start; i < params.size(); i++) {
            if (i > start) sb.append(", ");
            sb.append(params.get(i).name()).append(": ").append(params.get(i).type());
        }
        if (fn.variadic()) sb.append(", ...");
        sb.append("): ").append(fn.returnType());
        return sb.toString();
    }

    private MelExtensionRegistry.ExtensionFunction resolveFunction(PsiElement element) {
        if (element == null) return null;

        String name = element.getText();
        if (name == null || name.isBlank()) return null;

        PsiFile file = element.getContainingFile();
        if (file == null) return null;

        String fullText = file.getText();
        int startOffset = element.getTextRange().getStartOffset();
        if (startOffset > fullText.length()) return null;

        String before = fullText.substring(0, startOffset).stripTrailing();
        if (!before.endsWith(".")) return null;

        String beforeDot = before.substring(0, before.length() - 1).stripTrailing();
        String receiver = MelUtils.extractLastIdentifier(beforeDot);

        if (REGISTRY.isNamespace(receiver)) {
            for (var fn : REGISTRY.functionsForNamespace(receiver)) {
                if (fn.name().equals(name)) return fn;
            }
            return null;
        }

        // Not a namespace call - check dual-mode member functions (e.g. value.strftime(...))
        for (String namespace : REGISTRY.namespaces()) {
            for (var fn : REGISTRY.functionsForNamespace(namespace)) {
                if (!fn.receiverTypes().isEmpty() && fn.name().equals(name)) {
                    return fn;
                }
            }
        }
        return null;
    }
}
