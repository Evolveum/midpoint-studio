package com.evolveum.midpoint.studio.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        throw new NullPointerException("Here you go");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);


//        e.getPresentation().setVisible();
    }
}
