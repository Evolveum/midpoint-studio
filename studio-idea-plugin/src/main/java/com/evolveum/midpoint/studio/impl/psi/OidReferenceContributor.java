package com.evolveum.midpoint.studio.impl.psi;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns
                        .xmlAttributeValue()
                        .withParent(
                                XmlPatterns.xmlAttribute("oid")),
                new OidReferenceProvider());

        registrar.registerReferenceProvider(
                XmlPatterns
                        .xmlTag().withLocalName("oid"),
                new OidReferenceProvider());
    }
}
