package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteralContainer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesMultiHostInjector implements MultiHostInjector {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\$\\(.+?\\)");

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Project project = context.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        if (!(context instanceof PsiLanguageInjectionHost psiLangInjectionHost)) {
            return;
        }

        if (!MidPointUtils.isMidpointFile(psiLangInjectionHost.getContainingFile())) {
            return;
        }

        String str = context.getText();

        Matcher matcher = CODE_PATTERN.matcher(str);
        matcher.results().forEach(mr ->
                registrar
                        .startInjecting(SPropertiesLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(mr.start(), mr.end()))
                        .doneInjecting());
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Arrays.asList(XmlText.class, GrLiteralContainer.class);
    }
}

