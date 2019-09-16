package com.evolveum.midpoint.studio.impl.psi;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                xmlAttributeValue().withLocalName("oid"),
                new MidPointObjectReferenceProvider());
    }
}
