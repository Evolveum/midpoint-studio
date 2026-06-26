package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Provides code completion for MEL expressions:
 *   - bare identifiers and global functions at the top level
 *   - namespace functions after "namespace."
 *   - member functions after "expr."
 */
public class MelCompletionContributor extends CompletionContributor {

    public MelCompletionContributor() {
        extend(CompletionType.BASIC, psiElement(), new MelCompletionProvider());
    }

    private static class MelCompletionProvider extends CompletionProvider<CompletionParameters> {

        private static final MelExtensionRegistry REGISTRY = MelExtensionValidator.REGISTRY;

        private static final InsertHandler<LookupElement> PARENS_INSERT = (ctx, item) -> {
            EditorModificationUtil.insertStringAtCaret(ctx.getEditor(), "()", false, 1);
        };

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            String prefix = result.getPrefixMatcher().getPrefix();
            int offset = parameters.getOffset();
            String fullText = parameters.getOriginalFile().getText();
            String textBeforePrefix = fullText.substring(0, offset - prefix.length()).stripTrailing();

            if (textBeforePrefix.endsWith(".")) {
                String beforeDot = textBeforePrefix.substring(0, textBeforePrefix.length() - 1).stripTrailing();
                String receiver = extractLastIdentifier(beforeDot);

                if (REGISTRY.isNamespace(receiver)) {
                    addNamespaceFunctions(receiver, result);
                } else {
                    addMemberFunctions(result);
                }
            } else {
                addIdentifiers(result);
                addGlobalFunctions(result);
            }
        }

        private void addNamespaceFunctions(String namespace, CompletionResultSet result) {
            for (var fn : REGISTRY.functionsForNamespace(namespace)) {
                result.addElement(
                        LookupElementBuilder.create(fn.name())
                                .withTailText(formatParams(fn), true)
                                .withTypeText(fn.returnType())
                                .withInsertHandler(PARENS_INSERT));
            }
        }

        private void addMemberFunctions(CompletionResultSet result) {
            for (String name : MelExtensionValidator.KNOWN_MEMBER_FUNCTIONS) {
                result.addElement(
                        LookupElementBuilder.create(name)
                                .withInsertHandler(PARENS_INSERT));
            }
            // dual-mode extension functions (e.g. strftime on a timestamp receiver)
            for (String namespace : REGISTRY.namespaces()) {
                for (var fn : REGISTRY.functionsForNamespace(namespace)) {
                    if (!fn.receiverTypes().isEmpty()) {
                        result.addElement(
                                LookupElementBuilder.create(fn.name())
                                        .withTailText(formatParams(fn), true)
                                        .withTypeText(fn.returnType())
                                        .withInsertHandler(PARENS_INSERT));
                    }
                }
            }
        }

        private void addIdentifiers(CompletionResultSet result) {
            for (String name : MelExtensionValidator.KNOWN_IDENTIFIERS) {
                var builder = LookupElementBuilder.create(name);
                if (REGISTRY.isNamespace(name)) {
                    builder = builder.withTypeText("namespace");
                }
                result.addElement(builder);
            }
        }

        private void addGlobalFunctions(CompletionResultSet result) {
            for (String name : MelExtensionValidator.KNOWN_GLOBAL_FUNCTIONS) {
                result.addElement(
                        LookupElementBuilder.create(name)
                                .withInsertHandler(PARENS_INSERT));
            }
        }

        private String formatParams(MelExtensionRegistry.ExtensionFunction fn) {
            var sb = new StringBuilder("(");
            var params = fn.parameters();
            // skip the first parameter for dual-mode functions since it's the implicit receiver
            int start = fn.receiverTypes().isEmpty() ? 0 : 1;
            for (int i = start; i < params.size(); i++) {
                if (i > start) sb.append(", ");
                sb.append(params.get(i).name());
                sb.append(": ").append(params.get(i).type());
            }
            if (fn.variadic()) sb.append(", ...");
            sb.append(")");
            return sb.toString();
        }

        private String extractLastIdentifier(String text) {
            int end = text.length();
            int start = end;
            while (start > 0) {
                char c = text.charAt(start - 1);
                if (Character.isLetterOrDigit(c) || c == '_') {
                    start--;
                } else {
                    break;
                }
            }
            return text.substring(start, end);
        }
    }
}
