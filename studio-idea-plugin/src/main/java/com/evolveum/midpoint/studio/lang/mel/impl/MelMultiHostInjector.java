package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ScriptExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptActionExpressionType;
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

public class MelMultiHostInjector implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Project project = context.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        if (!(context instanceof XmlText text)) {
            return;
        }

        XmlTag code = text.getParentTag();
        if (!MidPointUtils.isTagMatchingNameOrType(code, ScriptExpressionEvaluatorType.F_CODE, DOMUtil.XSD_STRING)) {
            return;
        }

        XmlTag script = code.getParentTag();
        if (!isParentScript(code)) {
            return;
        }

        XmlTag language = MidPointUtils.findSubTag(script, ScriptExpressionEvaluatorType.F_LANGUAGE);
        if (language != null) {
            String languageName = language.getValue().getText();

            if (!"mel".equals(languageName)
                    && !"http://midpoint.evolveum.com/xml/ns/public/expression/language#mel".equals(languageName)) {
                return;
            }
        }

        registrar
                .startInjecting(MelLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength()))
                .doneInjecting();
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlText.class);
    }

    private boolean isParentScript(XmlTag code) {
        XmlTag parent = code.getParentTag();

        return MidPointUtils.isTagMatchingNameOrType(parent, ExecuteScriptActionExpressionType.F_SCRIPT, ScriptExpressionEvaluatorType.COMPLEX_TYPE)
                || MidPointUtils.isTagMatchingNameOrType(parent, SchemaConstantsGenerated.C_SCRIPT, ScriptExpressionEvaluatorType.COMPLEX_TYPE);
    }
}
