package com.evolveum.midpoint.studio.impl.assistant;

import com.evolveum.midpoint.studio.impl.MidpointCopilotService;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominik.
 */
public class StartupActivity implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        EditorActionManager actionManager = EditorActionManager.getInstance();
        TypedAction typedAction = actionManager.getTypedAction();

        TypedActionHandler originalHandler = typedAction.getHandler();
        MidpointCopilotService midpointCopilotService = new MidpointCopilotService();
        typedAction.setupHandler(new AssistantTypedHandler(originalHandler, midpointCopilotService));

        return CompletableFuture.completedFuture(null);
    }
}