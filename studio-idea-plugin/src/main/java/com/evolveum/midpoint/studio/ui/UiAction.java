package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Consumer;

public class UiAction extends AnAction {

    private final Consumer<AnActionEvent> actionPerformedConsumer;

    public UiAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable Consumer<AnActionEvent> actionPerformedConsumer) {
        this(text, null, actionPerformedConsumer);
    }

    public UiAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable Icon icon,
            @Nullable Consumer<AnActionEvent> actionPerformedConsumer) {

        super(text, null, icon);

        this.actionPerformedConsumer = actionPerformedConsumer;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (actionPerformedConsumer != null) {
            actionPerformedConsumer.accept(e);
        }
    }
}
