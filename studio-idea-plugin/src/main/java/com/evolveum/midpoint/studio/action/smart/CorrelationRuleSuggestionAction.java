/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.action.smart;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CorrelationRuleSuggestionAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        var presentation = e.getPresentation();
        presentation.setEnabled(isCorrelationObject(e.getData(CommonDataKeys.PSI_FILE)));
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isCorrelationObject(PsiFile psiFile) {
        return true;
    }
}
