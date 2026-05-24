package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;

public class DiscoverDocumentationStep extends StepAdapter {

    private final MidPointClient client;
    private final TaskStatusPoller taskStatusPoller;
    private final ConnectorGeneratorDataModel dataModel;
    private StepStateBadge.State state;

    private final DiscoverDocumentation panel;

    private boolean initialized = false;

    public DiscoverDocumentationStep(
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        this.client = client;
        this.taskStatusPoller = client.getProject().getService(TaskStatusPoller.class);;
        this.dataModel = dataModel;
        this.state = state;
        this.panel = new DiscoverDocumentation(dataModel);
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;
//            var token = client.submitOperationDiscoverDocumentation(dataModel.connectorDevelopmentType.getOid());
//            var loadingPanel = new LoadPanel("Identifying Documentation...",
//                    "Analyzing your target application details to locate the right documentation.",
//                    0
//            );
//
//            panel.add(loadingPanel);
//            taskStatusPoller.startPolling(() -> client.getStatusDiscoverDocumentation(token));
//
//            new Timer(1000, event -> {
//                ApplicationManager.getApplication().invokeLater(
//                        () -> loadingPanel.setElapsed(taskStatusPoller.getElapsedTime().toSeconds()));
//
//                if (taskStatusPoller.getStatus() != null
//                        && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
//                ) {
//                    taskStatusPoller.stopPolling();
//                    ((Timer) event.getSource()).stop();
//
//                    try {
//                        ProgressManager.getInstance().run(new Task.Backgroundable(client.getProject(),
//                                "Result DiscoverDocumentation"
//                        ) {
//                            @Override
//                            public void run(@NotNull ProgressIndicator progressIndicator) {
//                                progressIndicator.setIndeterminate(true);
//                                var result = client.getResultDiscoverDocumentation(token);
//                                ApplicationManager.getApplication().invokeLater(
//                                            () -> printResult(result), ModalityState.any());
//                            }
//                        });
//                    } catch (Exception e) {
//                        ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
//                                client.getProject(),
//                                e.getMessage(),
//                                "Error Result Discover Documentation"
//                        ));
//                    }
//                }
//            }).start();
        }

        super._init();
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }

    private void printResult(
            ConnDevDiscoverDocumentationResultType result
    ) {

        if (result == null) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                    client.getProject(),
                    "Result is null.",
                    "Error Result Discover Documentation"
            ));

            return;
        }

//        panel.removeAll();
//        panel.add(crateStepContent(result));
//        panel.revalidate();
//        panel.repaint();
    }

//    private JBPanel<?> crateStepContent(
//            @NotNull ConnDevDiscoverDocumentationResultType discoverDocumentationResultType
//    ) {
//
//        var mainPanel = new JBPanel<>();
//        JBLabel text = new JBLabel("Provide Integration Documentation");
//        text.setFont(text.getFont().deriveFont(Font.BOLD, 18f));
//
//        JBLabel subText = new JBLabel("""
//               Documentation helps the AI understand how your system communicates, which endpoints it exposes, and what data structures it uses. By analyzing this information, the AI can generate a more accurate and tailored connector for your integration.
//               """
//        );
//        subText.setBorder(JBUI.Borders.emptyBottom(15));
//
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//        mainPanel.add(text);
//        mainPanel.add(Box.createVerticalStrut(5));
//        mainPanel.add(subText);
//        mainPanel.add(createListDocs(discoverDocumentationResultType));
//
//        return mainPanel;
//    }

//    private JBScrollPane createListDocs(
//            @NotNull ConnDevDiscoverDocumentationResultType discoverDocumentationResultType
//    ) {
//        JBPanel<?> list = new JBPanel<>();
//        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
//        list.setAlignmentX(Component.LEFT_ALIGNMENT);
//        list.setOpaque(false);
//
//        discoverDocumentationResultType.getDocumentation().forEach(connDevDoc -> {
//            var item = createDocItem(
//                    connDevDoc,
//                    () -> System.out.println("View clicked"),
//                    () -> System.out.println("Delete clicked")
//            );
//
//            list.add(item);
//        });
//
//        JBScrollPane scrollPane = new JBScrollPane(list);
//        scrollPane.setBorder(JBUI.Borders.empty());
//
//        return scrollPane;
//    }
//
//    private JPanel createDocItem(
//            ConnDevDocumentationSourceType connDevDocumentationSourceType,
//            Runnable onView,
//            Runnable onDelete
//    ) {
//        JPanel row = new JPanel(new BorderLayout(10, 0));
//
//        row.setPreferredSize(new Dimension(600, 150));
//        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
//        row.setBorder(
//                BorderFactory.createCompoundBorder(
//                        BorderFactory.createLineBorder(com.intellij.ui.JBColor.border(), 1),
//                        com.intellij.util.ui.JBUI.Borders.empty(8)
//                )
//        );
//
//        JBCheckBox checkBox = new JBCheckBox();
//        checkBox.setVerticalAlignment(SwingConstants.TOP);
//        row.add(checkBox, BorderLayout.WEST);
//
//        JPanel content = new JPanel();
//        content.setLayout(new BorderLayout());
//        content.setOpaque(false);
//
//        JBLabel title = new JBLabel(connDevDocumentationSourceType.getName());
//        title.setFont(JBFont.h2());
//        title.setHorizontalAlignment(SwingConstants.LEFT);
//
//        JTextArea text = new JTextArea(connDevDocumentationSourceType.getDescription());
//        text.setLineWrap(true);
//        text.setWrapStyleWord(true);
//        text.setFont(JBFont.regular());
//        text.setEditable(false);
//        text.setOpaque(false);
//        text.setBorder(null);
//
//        JBScrollPane textScroll = new JBScrollPane(text);
//        textScroll.setBorder(null);
//        textScroll.setPreferredSize(new Dimension(120, 50));
//        textScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//
//        JBLabel uri = new JBLabel(connDevDocumentationSourceType.getUri());
//        uri.setFont(JBFont.regular());
//        uri.setHorizontalAlignment(SwingConstants.LEFT);
//
//        content.add(title, BorderLayout.NORTH);
//        content.add(textScroll, BorderLayout.CENTER);
//        content.add(uri, BorderLayout.CENTER);
//
//        JPanel actionsPanel = new JPanel();
//        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));
//
//        JButton viewButton = new JButton(AllIcons.Actions.Preview);
//        viewButton.setToolTipText("View");
//        viewButton.setBorderPainted(false);
//        viewButton.setContentAreaFilled(false);
//        viewButton.addActionListener(e -> onView.run());
//        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        JButton deleteButton = new JButton(AllIcons.Actions.GC);
//        deleteButton.setToolTipText("Delete");
//        deleteButton.setBorderPainted(false);
//        deleteButton.setContentAreaFilled(false);
//        deleteButton.addActionListener(e -> onDelete.run());
//        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        actionsPanel.add(viewButton);
//        actionsPanel.add(Box.createHorizontalStrut(5));
//        actionsPanel.add(deleteButton);
//
//        row.add(actionsPanel, BorderLayout.EAST);
//        row.add(content, BorderLayout.CENTER);
//
//        return row;
//    }
}
