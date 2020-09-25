package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetActionGroup extends DefaultActionGroup {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (e.getProject() == null) {
            return;
        }

        boolean hasFacet = MidPointUtils.hasMidPointFacet(e.getProject());
        e.getPresentation().setVisible(hasFacet);
    }
}
