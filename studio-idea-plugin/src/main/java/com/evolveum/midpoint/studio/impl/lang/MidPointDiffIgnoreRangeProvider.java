package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.lang.DiffIgnoredRangeProvider;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointDiffIgnoreRangeProvider implements DiffIgnoredRangeProvider {

    @NotNull
    @Override
    public String getDescription() {
        return "Ignores container ids";
    }

    @Override
    public boolean accepts(@Nullable Project project, @NotNull DiffContent content) {
        return true;
    }

    @NotNull
    @Override
    public List<TextRange> getIgnoredRanges(@Nullable Project project, @NotNull CharSequence text, @NotNull DiffContent content) {
        return ReadAction.compute(() -> {
            List<TextRange> result = new ArrayList<>();
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("", XMLLanguage.INSTANCE, text);

            psiFile.accept(new PsiElementVisitor() {

                @Override
                public void visitElement(PsiElement element) {
                    if (element.getTextLength() == 0) return;

                    if (isIgnored(element)) {
                        result.add(element.getTextRange());
                    } else {
                        element.acceptChildren(this);
                    }
                }
            });
            return result;
        });
    }

    private static boolean isIgnored(@NotNull PsiElement element) {
        if (element instanceof PsiWhiteSpace) return true;
        if (element instanceof XmlAttribute) {
            XmlAttribute attr = (XmlAttribute) element;
            if ("id".equalsIgnoreCase(attr.getLocalName())) {
                return true;
            }
        }
        return false;
    }
}
