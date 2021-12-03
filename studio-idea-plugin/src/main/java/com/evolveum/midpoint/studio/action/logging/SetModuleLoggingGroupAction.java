package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class SetModuleLoggingGroupAction extends DefaultActionGroup implements DumbAware {

    public SetModuleLoggingGroupAction(ModuleLogger logger) {
        add(new SetBasicLoggerAction(logger, LoggingLevelType.INFO));
        add(new SetBasicLoggerAction(logger, LoggingLevelType.DEBUG));
        add(new SetBasicLoggerAction(logger, LoggingLevelType.TRACE));
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        if (evt.getProject() == null) {
            return;
        }

        boolean hasFacet = MidPointUtils.hasMidPointFacet(evt.getProject());
        if (!hasFacet) {
            evt.getPresentation().setVisible(false);
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());

        evt.getPresentation().setEnabled(em.getSelected() != null);
    }
}
