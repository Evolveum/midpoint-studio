package com.evolveum.midpoint.studio.ui.assistant;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class AssistantToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatAssistant chatAssistant = new ChatAssistant();
        toolWindow.getComponent().add(chatAssistant.getContent());

//        toolWindow.getComponent().addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                int newWidth = toolWindow.getComponent().getWidth();
//                for (Component comp :  chat.getContent().getComponents()) {
//                    if (comp instanceof JPanel) {
//                        int bubbleWidth = newWidth - 40;
//                        comp.setMaximumSize(new Dimension(bubbleWidth, Integer.MAX_VALUE));
//                        comp.setPreferredSize(new Dimension(bubbleWidth, comp.getPreferredSize().height));
//                    }
//                }
//            }
//        });
    }
}
