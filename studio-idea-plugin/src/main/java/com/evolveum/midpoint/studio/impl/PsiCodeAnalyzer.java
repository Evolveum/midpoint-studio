package com.evolveum.midpoint.studio.impl;

import com.intellij.psi.*;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class PsiCodeAnalyzer {

    public static void analyzeCode(Project project, String code, String languageId) {
        Language language = Language.findLanguageByID(languageId.toUpperCase());
        if (language == null) return;

        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("snippet." + languageId, language, code);

        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitComment(@NotNull PsiComment comment) {
                System.out.println("Comment: " + comment.getText());
            }

            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                // optionally log other elements
            }
        });
    }
}
