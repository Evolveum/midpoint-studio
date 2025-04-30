package com.evolveum.midpoint.studio.impl.assistant;

import com.evolveum.midpoint.studio.impl.MidpointCopilotService;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominik.
 */
public class ConnectorAssistantActivity implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor virtualFile = fileEditorManager.getSelectedEditor();

        if (virtualFile != null && virtualFile.getFile().getFileType().equals(GroovyFileType.GROOVY_FILE_TYPE)) {
            EditorActionManager actionManager = EditorActionManager.getInstance();
            TypedAction typedAction = actionManager.getTypedAction();

            TypedActionHandler originalHandler = typedAction.getHandler();
            MidpointCopilotService midpointCopilotService = new MidpointCopilotService();
            typedAction.setupHandler(new AssistantTypedHandler(originalHandler, midpointCopilotService));
        }

        return CompletableFuture.completedFuture(null);
    }
}