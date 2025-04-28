package com.evolveum.midpoint.studio.ui.assistant;

import com.evolveum.midpoint.studio.impl.MarkdownParser;
import com.evolveum.midpoint.studio.impl.MidpointCopilotService;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominik.
 *
 */
public class ChatAssistant {

    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JPanel chatArea = new JPanel();
    private final JTextField inputField = new JTextField();

    private final MidpointCopilotService midpointCopilotService = new MidpointCopilotService();

    public ChatAssistant() {
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
            addMessage("You: " + userInput + "\n", true);
            inputField.setText("");

            JLabel loadingLabel = new JLabel("Thinking...");
            loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            loadingLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            loadingLabel.setVisible(true);
            JPanel loadingWrapper = new JPanel(new BorderLayout());
            loadingWrapper.setBackground(chatArea.getBackground());
            loadingWrapper.add(loadingLabel, BorderLayout.CENTER);

            chatArea.add(loadingWrapper);
            chatArea.revalidate();
            chatArea.repaint();

            CompletableFuture.supplyAsync(() -> generateFakeResponse(userInput)).thenAccept(response -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.remove(loadingWrapper);
                        addMessage("AI: " + response + "\n", false);
                    });
                });
            });
        }
    }

    private JPanel createBlock(String markdown, boolean isUser) {
        JPanel wrapper = new JPanel();
//        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
//        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JTextPane textPane = new JTextPane();
        int width = 400;

        MarkdownParser.extractBlocks(markdown).forEach(blockContent -> {
            textPane.setText(blockContent.text());
//            textPane.setText(blockContent.language());
//            textPane.setText(blockContent.code());
        });
//        textPane.add(Box.createVerticalStrut(10));

        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(null);

//        textPane.setSize(new Dimension(width, Short.MAX_VALUE));
//        Dimension d = textPane.getPreferredSize();
//        textPane.setPreferredSize(new Dimension(width, d.height));
//        textPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

        wrapper.add(textPane);
        wrapper.add(Box.createVerticalStrut(0));
        wrapper.setSize(new Dimension(width, Short.MAX_VALUE));
        Dimension dd = wrapper.getPreferredSize();
        wrapper.setPreferredSize(new Dimension(width, dd.height));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, dd.height));

        return wrapper;
    }

    public void addMessage(String text, boolean isUser) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.setBackground(UIManager.getColor("Panel.background"));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        messagePanel.add(label);

        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, messagePanel.getPreferredSize().height));

        chatArea.add(messagePanel);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        chatArea.add(separator);
        chatArea.revalidate();
        chatArea.repaint();
    }

    private String generateFakeResponse(String input) {
        return midpointCopilotService.generate(input, "generate");
    }

    public JComponent getContent() {
        return contentPanel;
    }
}
