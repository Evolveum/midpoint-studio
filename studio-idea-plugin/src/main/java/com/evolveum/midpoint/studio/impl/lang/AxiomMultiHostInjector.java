package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.studio.axiom.query.AxiomQueryLanguage;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomMultiHostInjector implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Project project = context.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        if (!(context instanceof XmlText)) {
            return;
        }

        XmlText text = (XmlText) context;
        XmlTag tag = text.getParentTag();
        if (tag == null || !MidPointUtils.isTagMatchingNameOrType(tag, SearchFilterType.F_TEXT, DOMUtil.XSD_STRING)) {
            return;
        }

        registrar
                .startInjecting(AxiomQueryLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength()))
                .doneInjecting();
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlText.class);
    }
}
