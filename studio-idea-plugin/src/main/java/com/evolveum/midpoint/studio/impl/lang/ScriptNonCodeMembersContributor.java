package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import groovy.lang.Script;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.GrDynamicImplicitProperty;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtilKt;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ScriptNonCodeMembersContributor extends NonCodeMembersContributor {

    @Override
    public void processDynamicElements(@NotNull PsiType qualifierType, PsiClass aClass,
                                       @NotNull PsiScopeProcessor processor, @NotNull PsiElement place,
                                       @NotNull ResolveState state) {
        if (aClass == null) {
            return;
        }

        PsiClass parent = aClass.getSuperClass();
        if (parent == null || !Script.class.getName().equals(parent.getQualifiedName())) {
            return;
        }

        // we want to create member properties only for parent script

        boolean shouldProcessProperties = ResolveUtilKt.shouldProcessProperties(processor);
        if (!shouldProcessProperties) {
            return;
        }

        PsiManager psiManager = PsiManager.getInstance(aClass.getProject());

        for (MidPointExpressionVariables v : MidPointExpressionVariables.values()) {
            Class type = getVariableType(v);

            PsiVariable variable = new GrDynamicImplicitProperty(psiManager, v.getVariable(),
                    type.getName(), aClass.getQualifiedName());

            if (!ResolveUtil.processElement(processor, variable, state)) {
                return;
            }
        }
    }

    private Class getVariableType(MidPointExpressionVariables var) {
        if (var.getType() != null) {
            return var.getType();
        }

        return Object.class;    // todo improve for "input" variable and/or variables that can contain subtypes
    }
}
