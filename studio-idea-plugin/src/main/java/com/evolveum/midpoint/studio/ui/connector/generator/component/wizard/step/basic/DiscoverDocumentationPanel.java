package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.action.task.DiscoverDocumentationTask;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.LoadingPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDocumentationSourceType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DiscoverDocumentationPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    private final ConnectorGeneratorDialogContext dialogContext;

    public DiscoverDocumentationPanel(ConnectorGeneratorDialogContext dialogContext) {
        this.dialogContext = dialogContext;
        setLayout(new BorderLayout());
        createDiscoverDocumentationPanel();
    }

    private void createDiscoverDocumentationPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(JBUI.Borders.empty(15));
        add(createTopBanner(), BorderLayout.NORTH);

        JLabel aiBadge = new JLabel(" % AI ");
        aiBadge.setOpaque(true);
        aiBadge.setBackground(new Color(230, 220, 240));
        aiBadge.setForeground(new Color(150, 50, 170));
        aiBadge.setFont(aiBadge.getFont().deriveFont(Font.BOLD, 10));
        aiBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        add(aiBadge);
    }

    private JPanel createTopBanner() {
        JPanel topPanel = new JBPanel<>();

        JLabel description = new JLabel(
                "Tell us which application you want to connect to. Based on this information, the system will identify the target and locate appropriate documentation."
        );
        description.setBorder(JBUI.Borders.emptyBottom(15));

        JLabel header = new JLabel("Identify the Target Application");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(description);

        return topPanel;
    }

    private JPanel createListDocs(@NotNull ConnDevDiscoverDocumentationResultType discoverDocumentationResultType) {
        JPanel itemListPanel = new JPanel();
        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        itemListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemListPanel.setOpaque(false);

        discoverDocumentationResultType.getDocumentation().forEach(connDevDoc -> {
            itemListPanel.add(createDocItem(
                    connDevDoc,
                    () -> System.out.println("View clicked"),
                    () -> System.out.println("Delete clicked")
            ));
        });

        return itemListPanel;
    }

    private JPanel createDocItem(
            ConnDevDocumentationSourceType connDevDocumentationSourceType,
            Runnable onView,
            Runnable onDelete
    ) {
        JPanel container = new JBPanel<>(new BorderLayout(10, 5));

        JBCheckBox checkBox = new JBCheckBox();
        container.add(checkBox, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JBLabel titleLabel = new JBLabel(connDevDocumentationSourceType.getName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        textPanel.add(titleLabel);

        JBLabel descriptionLabel = new JBLabel(connDevDocumentationSourceType.getDescription());
        textPanel.add(descriptionLabel);
        container.add(textPanel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));

        JButton viewButton = new JButton(AllIcons.Actions.Preview);
        viewButton.setToolTipText("View");
        viewButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.addActionListener(e -> onView.run());

        JButton deleteButton = new JButton(AllIcons.Actions.GC);
        deleteButton.setToolTipText("Delete");
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.addActionListener(e -> onDelete.run());

        actionsPanel.add(viewButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(deleteButton);
        container.add(actionsPanel, BorderLayout.EAST);

        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return container;
    }

    @Override
    public void afterChangeAction() {
        if (dialogContext.getConnDevDiscoverDocumentationResultType() == null &&
                dialogContext.getConnectorDevelopmentType() != null)
        {
            var loadingPanel = new LoadingPanel();
            add(loadingPanel);

            new DiscoverDocumentationTask(
                    dialogContext.getProject(),
                    () -> DataContext.EMPTY_CONTEXT,
                    dialogContext.getConnectorDevelopmentType().getOid()
            ) {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    var discoverDocumentation = getConnDevDiscoverDocumentationResultType();
                    dialogContext.setConnDevDiscoverDocumentationResultType(discoverDocumentation);
                    add(createListDocs(discoverDocumentation));
                    remove(loadingPanel);
                    revalidate();
                    repaint();
                }

                @Override
                public void onThrowable(@NotNull Throwable error) {
                    super.onThrowable(error);
                }

                @Override
                public void onFinished() {
                    super.onFinished();
                }
            }.queue();
        }
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
