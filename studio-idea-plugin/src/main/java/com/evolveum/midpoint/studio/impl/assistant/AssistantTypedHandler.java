package com.evolveum.midpoint.studio.impl.assistant;

import com.evolveum.midpoint.studio.impl.MidpointCopilotService;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class AssistantTypedHandler implements TypedActionHandler {

    private final TypedActionHandler originalHandler;
    private final MidpointCopilotService midpointCopilotService;

    public AssistantTypedHandler(TypedActionHandler originalHandler, MidpointCopilotService midpointCopilotService) {
        this.originalHandler = originalHandler;
        this.midpointCopilotService = midpointCopilotService;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        originalHandler.execute(editor, charTyped, dataContext);
        fetchSuggestionAsync(editor, getContext(editor));
    }

    private String getContext(Editor editor) {
        Document doc = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        int start = Math.max(0, offset - 500); // last 500 chars limitations for prototype
        return doc.getText(new TextRange(start, offset));
    }

    private void fetchSuggestionAsync(Editor editor, String context) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String suggestion = callAI(context);

            if (context.equals("def ")) {
                ApplicationManager.getApplication().invokeLater(() ->
                        showSuggestion(editor, ""));
            } else {
                if (!suggestion.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() ->
                            showSuggestion(editor, suggestion));
                }
            }
        });
    }

    private void showSuggestion(Editor editor, String suggestion) {
        InlayModel inlayModel = editor.getInlayModel();
        int offset = editor.getCaretModel().getOffset();

        inlayModel.addAfterLineEndElement(offset, true,
                new SimpleInlayRenderer(suggestion));
    }

    private String callAI(String context) {
        return this.midpointCopilotService.generate(context, "static-groovy-script");
    }
}