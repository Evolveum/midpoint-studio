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
                .append(signatures(fn))
                .append(DocumentationMarkup.DEFINITION_END);

        // Overloads may carry different documentation (e.g. the global form of a function
        // documents its null behavior); show each distinct text once.
        var docs = fn.overloads().stream()
                .map(MelExtensionRegistry.Overload::documentation)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .toList();
        if (!docs.isEmpty()) {
            sb.append(DocumentationMarkup.CONTENT_START)
                    .append(String.join("<p>", docs))
                    .append(DocumentationMarkup.CONTENT_END);
        }

        return sb.toString();
    }

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        var fn = resolveFunction(element);
        return fn == null ? null : signatures(fn);
    }

    /**
     * All overload signatures, one per line. Member overloads are rendered receiver-style
     * ("string.isBlank(): bool"), global overloads call-style ("isBlank(string): bool"),
     * so variants that would otherwise render identically stay distinguishable.
     */
    private String signatures(MelExtensionRegistry.ExtensionFunction fn) {
        var lines = new java.util.LinkedHashSet<String>();
        for (var overload : fn.overloads()) {
            lines.add(signature(fn, overload));
        }
        return String.join("<br/>", lines);
    }

    private String signature(MelExtensionRegistry.ExtensionFunction fn, MelExtensionRegistry.Overload overload) {
        var sb = new StringBuilder();
        var params = overload.parameterTypes();
        int start = 0;
        if (overload.member() && !params.isEmpty()) {
            sb.append(params.get(0)).append('.');
            start = 1;
        }
        if (fn.namespace() != null) {
            sb.append(fn.namespace()).append('.');
        }
        sb.append(fn.name()).append("(");
        for (int i = start; i < params.size(); i++) {
            if (i > start) sb.append(", ");
            sb.append(params.get(i));
        }
        sb.append("): ").append(overload.returnType());
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

        // Not a namespace call - check bare member functions (e.g. value.isBlank())
        var fn = REGISTRY.bareFunction(name);
        if (fn != null && fn.memberCallable()) {
            return fn;
        }
        return null;
    }
}
