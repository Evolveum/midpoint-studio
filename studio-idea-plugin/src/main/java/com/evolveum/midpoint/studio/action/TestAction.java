package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.cache.ConnectorXmlSchemaCacheService;
import com.evolveum.midpoint.studio.impl.cache.XmlSchemaCacheService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }

        Project project = e.getProject();

        project.getService(ConnectorXmlSchemaCacheService.class).clear();
        project.getService(XmlSchemaCacheService.class).clear();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);


//        e.getPresentation().setVisible();
    }
}
