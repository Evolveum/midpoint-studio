package com.evolveum.midpoint.studio.action.logging;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RepositoryLoggingGroupAction extends SetModuleLoggingGroupAction {

    public RepositoryLoggingGroupAction() {
        super("com.evolveum.midpoint.repo");
    }
}
