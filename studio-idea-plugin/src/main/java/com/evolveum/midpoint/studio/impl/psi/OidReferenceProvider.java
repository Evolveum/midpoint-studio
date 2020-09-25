package com.evolveum.midpoint.studio.impl.psi;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (MidPointUtils.isItObjectTypeOidAttribute(element)) {
            return new PsiReference[]{};
        }

        return new PsiReference[]{new OidReference((XmlAttributeValue) element)};
    }
}
