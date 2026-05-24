package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ConnectorGeneratorWizardAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        new ConnectorGeneratorWizard(anActionEvent.getProject()).show();
    }
}
