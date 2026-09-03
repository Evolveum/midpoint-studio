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
 * - bare identifiers and global functions at the top level
 * - namespace functions after "namespace."
 * - member functions after "expr."
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
                String receiver = MelUtils.extractLastIdentifier(beforeDot);

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
                                .withTailText(formatParams(fn, false) + tailDescription(fn), true)
                                .withTypeText(fn.returnType())
                                .withInsertHandler(PARENS_INSERT));
            }
        }

        private void addMemberFunctions(CompletionResultSet result) {
            for (var fn : REGISTRY.memberFunctions()) {
                result.addElement(
                        LookupElementBuilder.create(fn.name())
                                .withTailText(formatParams(fn, true) + tailDescription(fn), true)
                                .withTypeText(fn.returnType())
                                .withInsertHandler(PARENS_INSERT));
            }
            for (String name : MelExtensionValidator.EXTRA_MEMBER_FUNCTIONS) {
                result.addElement(
                        LookupElementBuilder.create(name)
                                .withInsertHandler(PARENS_INSERT));
            }
            for (String name : REGISTRY.macroNames()) {
                if (!name.equals("has")) { // has(...) is global-only
                    result.addElement(
                            LookupElementBuilder.create(name)
                                    .withTypeText("macro")
                                    .withInsertHandler(PARENS_INSERT));
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
            for (var fn : REGISTRY.globalFunctions()) {
                result.addElement(
                        LookupElementBuilder.create(fn.name())
                                .withTailText(formatParams(fn, false) + tailDescription(fn), true)
                                .withTypeText(fn.returnType())
                                .withInsertHandler(PARENS_INSERT));
            }
            for (String name : REGISTRY.standardFunctionNames()) {
                if (REGISTRY.isValidGlobalCall(name)) {
                    continue; // already offered above with a signature (e.g. string, size, matches)
                }
                result.addElement(
                        LookupElementBuilder.create(name)
                                .withInsertHandler(PARENS_INSERT));
            }
            result.addElement(
                    LookupElementBuilder.create("has")
                            .withTypeText("macro")
                            .withInsertHandler(PARENS_INSERT));
        }

        /**
         * Parameter list for the completion tail, using the first overload matching the
         * call form being completed (member vs global); other overloads of that form are
         * indicated by a "(+N)" marker.
         */
        private String formatParams(MelExtensionRegistry.ExtensionFunction fn, boolean memberCall) {
            var overloads = fn.overloads().stream()
                    .filter(o -> o.member() == memberCall)
                    .toList();
            if (overloads.isEmpty()) {
                overloads = fn.overloads();
            }
            var overload = overloads.get(0);
            var sb = new StringBuilder("(");
            var params = overload.parameterTypes();
            // for member overloads the first parameter is the implicit receiver
            int start = overload.member() ? 1 : 0;
            for (int i = start; i < params.size(); i++) {
                if (i > start) sb.append(", ");
                sb.append(params.get(i));
            }
            sb.append(")");
            if (overloads.size() > 1) {
                sb.append(" (+").append(overloads.size() - 1).append(")");
            }
            return sb.toString();
        }

        /**
         * Returns " - <first sentence of documentation>" for use as additional, grayed-out
         * tail text in the completion popup, or an empty string if no documentation is available.
         */
        private String tailDescription(MelExtensionRegistry.ExtensionFunction fn) {
            String doc = fn.documentation();
            if (doc == null || doc.isBlank()) return "";

            int dot = doc.indexOf(". ");
            String firstSentence = dot >= 0 ? doc.substring(0, dot + 1) : doc;
            return "  - " + firstSentence;
        }
    }
}
