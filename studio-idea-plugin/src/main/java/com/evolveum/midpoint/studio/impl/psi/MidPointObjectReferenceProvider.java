package com.evolveum.midpoint.studio.impl.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointObjectReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        // todo create reference to object - if parent was reference ref
        return new PsiReference[]{new MPReference(element)};
    }

    private static class MPReference implements PsiReference {

        private PsiElement element;

        public MPReference(PsiElement element) {
            this.element = element;
        }

        @NotNull
        @Override
        public PsiElement getElement() {
            return element;
        }

        @NotNull
        @Override
        public TextRange getRangeInElement() {
            return element.getTextRange();
        }

        @Nullable
        @Override
        public PsiElement resolve() {
            return element.getContainingFile();
        }

        @NotNull
        @Override
        public String getCanonicalText() {
            return "aaa";
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
            return null;
        }

        @Override
        public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
            return null;
        }

        @Override
        public boolean isReferenceTo(@NotNull PsiElement element) {
            return this.element.getManager().areElementsEquivalent(resolve(), element);
        }

        @Override
        public boolean isSoft() {
            return false;
        }
    }
}
