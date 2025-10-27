package com.evolveum.midpoint.studio.ui.prototype.vaia.assistant;

import com.evolveum.midpoint.studio.impl.MarkdownParser;
import com.evolveum.midpoint.studio.impl.MidpointCopilotService;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominik.
 *
 */
public class ChatAssistant {

    private final Project project;
    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JPanel chatArea = new JPanel();
    private final JTextField inputField = new JTextField();

    private final MidpointCopilotService midpointCopilotService = new MidpointCopilotService();

    public ChatAssistant(Project project) {
        this.project = project;

        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        JButton sendButton = new JButton("Send");
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);

        contentPanel.add(inputPanel, BorderLayout.SOUTH);
        inputPanel.add(sendButton, BorderLayout.EAST);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            addMessage(userInput, true);
            inputField.setText("");

            JLabel loadingLabel = new JLabel("Thinking...");
            loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel loadingWrapper = new JPanel(new BorderLayout());
            loadingWrapper.setLayout(new BoxLayout(loadingWrapper, BoxLayout.Y_AXIS));
            loadingWrapper.setBorder(BorderFactory.createEmptyBorder(300, 600, 600, 300));
            loadingWrapper.add(loadingLabel);

            chatArea.add(loadingWrapper);
            chatArea.revalidate();
            chatArea.repaint();

            CompletableFuture.supplyAsync(() -> generateResponse(userInput)).thenAccept(response -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.remove(loadingWrapper);
                        addMessage(response, false);
                    });
                });
            });
        }
    }

    public void addMessage(String text, boolean isUser) {
        int maxWidth = 600;

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(UIManager.getColor("Panel.background"));
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        wrapper.setBackground(isUser
                ? ChatColors.MESSAGE_BACKGROUND_USER
                : ChatColors.MESSAGE_BACKGROUND_BOT);

        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setForeground(ChatColors.MESSAGE_TEXT_COLOR);
        label.setBackground(isUser
                ? ChatColors.MESSAGE_BACKGROUND_USER
                : ChatColors.MESSAGE_BACKGROUND_BOT);

        if (isUser) {
            label.setText("<html><b>" + text + "</b></html>");
            wrapper.add(label);
        } else {
            MarkdownParser.extractBlocks(text).forEach(blockContent -> {
                if (blockContent.html() != null) {
                    JLabel AILabel = new JLabel();
                    AILabel.setOpaque(true);
                    AILabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                    AILabel.setForeground(ChatColors.MESSAGE_TEXT_COLOR);
                    AILabel.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
                    AILabel.setVerticalAlignment(1);
                    AILabel.setText("<html>" + blockContent.html() + "</html>");
                    wrapper.add(AILabel);
                }

                if (blockContent.language() != null) {
                    wrapper.add(new JLabel("<html><b>Language: </b>" + blockContent.language() + "</html>"));
                }

                if (blockContent.code() != null) {
                    wrapper.add(createCodeSnippetBlock(blockContent.code(), project, JavaLanguage.INSTANCE.getAssociatedFileType()));
                }
            });
        }

//        label.setSize(maxWidth, Integer.MAX_VALUE);
//        Dimension preferredSize = label.getPreferredSize();
//        label.setPreferredSize(new Dimension(maxWidth, preferredSize.height));
//        wrapper.setMaximumSize(new Dimension(maxWidth, preferredSize.height));
//        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        chatArea.add(wrapper);
        chatArea.revalidate();
        chatArea.repaint();
    }

    public JComponent createCodeSnippetBlock(String code, Project project, FileType fileType) {
        Document document = EditorFactory.getInstance().createDocument(code);
        EditorTextField editorField = new EditorTextField(document, project, fileType, true, false);
//        editorField.setMaximumSize(new Dimension(500, 200));

//        if (editorField instanceof EditorEx editorEx) {
//            editorEx.setViewer(true);
//            editorEx.setCaretEnabled(false);
//            editorEx.setHorizontalScrollbarVisible(true);
//            editorEx.setVerticalScrollbarVisible(true);
//        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(editorField);
        wrapper.setPreferredSize(new Dimension(500, 200));
        return wrapper;
    }

    private String generateResponse(String input) {
        return midpointCopilotService.generate(input, "generate");
    }

    public JComponent getContent() {
        return contentPanel;
    }
}
