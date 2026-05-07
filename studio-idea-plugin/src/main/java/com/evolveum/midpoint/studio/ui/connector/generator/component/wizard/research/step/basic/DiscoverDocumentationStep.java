package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.ConnectorGeneratorWizardData;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDocumentationSourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiscoverDocumentationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorWizardData dataModel;
    private final JBPanel<?> panel = new JBPanel<>();

    private final ScheduledExecutorService executor = AppExecutorUtil.getAppScheduledExecutorService();
    private ScheduledFuture<?> future;
    private long startTime;

    private boolean initialized = false;

    public DiscoverDocumentationStep(MidPointClient client, ConnectorGeneratorWizardData dataModel) {
        this.client = client;
        this.dataModel = dataModel;
        panel.setName("Discover Documentation");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;

            var statusLabel = new JBLabel("Waiting...");
            panel.add(statusLabel);
            startPolling(dataModel.connectorDevelopmentType.getOid(), statusLabel);
        }

        super._init();
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    private void startPolling(String connectorDevelopmentOperationOid, JBLabel statusLabel) {
        var token = client.submitOperationDiscoverDocumentation(connectorDevelopmentOperationOid);
        startTime = System.currentTimeMillis();

        future = executor.scheduleWithFixedDelay(() -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            var status = client.getStatusDiscoverDocumentation(token);

            if (OperationResultStatusType.SUCCESS.equals(status)) {
                var result = client.getResultDiscoverDocumentation(token);
                panel.add(createTopBanner());
                panel.add(createListDocs(result));
            } else {
                statusLabel.setText(
                        String.format(
                                "Waiting for endpoint... Elapsed time: %dm %ds",
                                elapsed / 60,
                                elapsed % 60
                        )
                );
            }
            statusLabel.revalidate();
            statusLabel.repaint();

            if (OperationResultStatusType.SUCCESS.equals(status) && future != null) {
                future.cancel(false);
            }

        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private JBPanel<?> createTopBanner() {
        JBPanel<?> topPanel = new JBPanel<>();

        JBLabel header = new JBLabel("Identify the target application");
        header.setFont(JBFont.h1());

        JBLabel description = new JBLabel("""
                Tell us which application you want to connect to. Based on this information,\s
                the system will identify the target and locate appropriate documentation.
               """
        );
        description.setFont(JBFont.h3());
        description.setBorder(JBUI.Borders.emptyBottom(30));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(description);

        return topPanel;
    }

    private JBScrollPane createListDocs(@NotNull ConnDevDiscoverDocumentationResultType discoverDocumentationResultType) {
        JBPanel<?> list = new JBPanel<>();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.setOpaque(false);

        discoverDocumentationResultType.getDocumentation().forEach(connDevDoc -> {
            var item = createDocItem(
                    connDevDoc,
                    () -> System.out.println("View clicked"),
                    () -> System.out.println("Delete clicked")
            );

            list.add(item);
        });

        JBScrollPane scrollPane = new JBScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        return scrollPane;
    }

    private JPanel createDocItem(
            ConnDevDocumentationSourceType connDevDocumentationSourceType,
            Runnable onView,
            Runnable onDelete
    ) {
        JPanel row = new JPanel(new BorderLayout(10, 0));

        row.setPreferredSize(new Dimension(600, 150));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(com.intellij.ui.JBColor.border(), 1),
                        com.intellij.util.ui.JBUI.Borders.empty(8)
                )
        );

        JBCheckBox checkBox = new JBCheckBox();
        checkBox.setVerticalAlignment(SwingConstants.TOP);
        row.add(checkBox, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setOpaque(false);

        JBLabel title = new JBLabel(connDevDocumentationSourceType.getName());
        title.setFont(JBFont.h2());
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JTextArea text = new JTextArea(connDevDocumentationSourceType.getDescription());
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(JBFont.regular());
        text.setEditable(false);
        text.setOpaque(false);
        text.setBorder(null);

        JBScrollPane textScroll = new JBScrollPane(text);
        textScroll.setBorder(null);
        textScroll.setPreferredSize(new Dimension(120, 50));
        textScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JBLabel uri = new JBLabel(connDevDocumentationSourceType.getUri());
        uri.setFont(JBFont.regular());
        uri.setHorizontalAlignment(SwingConstants.LEFT);

        content.add(title, BorderLayout.NORTH);
        content.add(textScroll, BorderLayout.CENTER);
        content.add(uri, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));

        JButton viewButton = new JButton(AllIcons.Actions.Preview);
        viewButton.setToolTipText("View");
        viewButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.addActionListener(e -> onView.run());
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton deleteButton = new JButton(AllIcons.Actions.GC);
        deleteButton.setToolTipText("Delete");
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.addActionListener(e -> onDelete.run());
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionsPanel.add(viewButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(deleteButton);

        row.add(actionsPanel, BorderLayout.EAST);
        row.add(content, BorderLayout.CENTER);

        return row;
    }
}
