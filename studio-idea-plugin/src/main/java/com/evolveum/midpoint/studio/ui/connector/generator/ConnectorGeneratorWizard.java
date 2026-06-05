package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask;
import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ConnectorGeneratorWizard extends AbstractWizard<Step> {

    private final Logger log = Logger.getInstance(this.getClass());

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();

    private JBList<NavigationItem> stepNavigationItems;

    private final java.util.List<ConnectorGeneratorWizardStep> myWizardStepsList = new ArrayList<>();
    private final DefaultListModel<NavigationItem> visibleListModel = new DefaultListModel<>();
    private final List<Integer> navIndexToStepIndexMap = new ArrayList<>();

    public ConnectorGeneratorWizard(@NotNull MidPointClient client) {
        super("Connector Generator", client.getProject());
        this.client = client;
        buildSteps();
        getHelpButton().setVisible(false);
        setSize(1300, 600);
        init();
    }

    private void buildSteps() {
        myWizardStepsList.clear();

        myWizardStepsList.add(new WaitingBasicInfoConnectorStep(this, client, dataModel, StepStateBadge.State.NONE));
        myWizardStepsList.add(new BaseUrlConnectorStep(this, client, dataModel, StepStateBadge.State.NONE));

        myWizardStepsList.forEach(this::addStep);
        stepNavigationItems = new JBList<>(visibleListModel);

        updateNavigationMenuByLiveStates();
    }

    @Override
    protected @Nullable @NonNls String getHelpID() {
        return "";
    }

    @Override
    protected JComponent createCenterPanel() {
        final JBPanel<?> cellWrapperPanel = new JBPanel<>(new BorderLayout());
        final JLabel label = new JLabel();
        final StepStateBadge stateBadge = new StepStateBadge(StepStateBadge.State.NONE);

        cellWrapperPanel.add(label, BorderLayout.CENTER);
        cellWrapperPanel.add(stateBadge, BorderLayout.EAST);

        stepNavigationItems.setCellRenderer((
                list,
                value,
                index,
                isSelected,
                cellHasFocus
        ) -> {
            stateBadge.setState(value.state());
            label.setText(value.name());

            if (value.isHeader()) {
                cellWrapperPanel.setBorder(JBUI.Borders.empty(12, 10, 4, 10));
                label.setFont(UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(Font.BOLD));
                label.setForeground(UIUtil.getContextHelpForeground());
                stateBadge.setVisible(true);
            } else {
                cellWrapperPanel.setOpaque(true);
                cellWrapperPanel.setBorder(JBUI.Borders.empty(8, 22, 8, 10));
                stateBadge.setVisible(true);

                if (isSelected) {
                    label.setForeground(UIUtil.getListSelectionForeground(true));
                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scaleFontSize(14f)));
                } else {
                    label.setForeground(UIUtil.getLabelForeground());
                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.PLAIN, JBUI.scaleFontSize(14f)));
                }
            }
            return cellWrapperPanel;
        });

        stepNavigationItems.setBorder(JBUI.Borders.empty());
        stepNavigationItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepNavigationItems.setSelectionBackground(UIUtil.TRANSPARENT_COLOR);
        stepNavigationItems.setFixedCellHeight(-1);

        stepNavigationItems.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int listIndex = stepNavigationItems.getSelectedIndex();
                if (listIndex >= 0 && listIndex < navIndexToStepIndexMap.size()) {
                    int mappedWizardStepIndex = navIndexToStepIndexMap.get(listIndex);

                    if (mappedWizardStepIndex == -1) {
                        stepNavigationItems.setSelectedIndex(listIndex + 1);
                        return;
                    }

                    if (mappedWizardStepIndex != getCurrentStep() && mappedWizardStepIndex >= 0 && mappedWizardStepIndex < mySteps.size()) {
                        myCurrentStep = mappedWizardStepIndex;
                        updateStep();
                    }
                }
            }
        });

        JBSplitter splitter = new JBSplitter(false, 0.25f);
        splitter.setBorder(JBUI.Borders.empty());
        splitter.setFirstComponent(stepNavigationItems);

        JScrollPane mainPanel = ScrollPaneFactory.createScrollPane(super.createCenterPanel());
        mainPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mainPanel.setBorder(JBUI.Borders.empty());
        splitter.setSecondComponent(mainPanel);

        return splitter;
    }

    @Override
    protected void doNextAction() {
        super.doNextAction();
        updateNavigationMenuByLiveStates();
    }

    @Override
    protected void doPreviousAction() {
        super.doPreviousAction();
        stepNavigationItems.setSelectedIndex(getCurrentStep());
    }

    @Override
    protected boolean canGoNext() {
        return myWizardStepsList.get(getCurrentStep()).isCanGoNext();
    }

    @Override
    protected boolean canFinish() {
        return myWizardStepsList.get(getCurrentStep()).isCanGoNext();
    }

//    @Override
//    protected void updateStep() {
//        super.updateStep();
//
//        if (isLastStep()) {
//            getNextButton().setText("Download Connector Development");
//        }
//    }

    @Override
    protected void doOKAction() {


        MidPointUtils.publishNotification(client.getProject(), EncryptionService.NOTIFICATION_KEY, "Connector Generator",
                "Connector %s downloaded", NotificationType.INFORMATION);


        try {
            ProgressManager.getInstance().run(new DownloadConnectorDevelopmentTask(
                    client.getProject(),
                    client.getEnvironment(),
                    dataModel.connectorDevelopmentType.getName().getOrig().replace(":" , "."),
                    file -> ApplicationManager.getApplication().invokeLater(() -> showInfoNotificationWithAction(
                                    client,
                                    "Connector Generator",
                                    "Connector downloaded"
                            )
                    )));
        } catch (Exception e) {
            log.error(e);
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                    client.getProject(), e.getMessage(), "Error Create Connector"));
        }

        super.doOKAction();
    }

    public void updateNavigationMenuByLiveStates() {
        visibleListModel.clear();
        navIndexToStepIndexMap.clear();

        IntStream.range(0, myWizardStepsList.size()).forEach(
                i -> addStepIfStateNotNone(
                        i,
                        myWizardStepsList.get(i).getComponent().getName(),
                        myWizardStepsList.get(i).isHeader()
                ));

        synchronizeMenuHighlight();
    }

    private void addStepIfStateNotNone(int stepIndex, String stepDisplayName, boolean isHeader) {
        Step step = myWizardStepsList.get(stepIndex);

        if (step instanceof ConnectorGeneratorWizardStep generatorStep) {
            StepStateBadge.State currentState = generatorStep.getState();

            if (!currentState.equals(StepStateBadge.State.NONE)) {
                visibleListModel.addElement(new NavigationItem(stepDisplayName, currentState, isHeader));
                navIndexToStepIndexMap.add(stepIndex);
            }
        }
    }

    private void synchronizeMenuHighlight() {
        IntStream.range(0, navIndexToStepIndexMap.size())
                .filter(i -> navIndexToStepIndexMap.get(i) == getCurrentStep())
                .findFirst()
                .ifPresent(stepNavigationItems::setSelectedIndex);
    }

    public static void showInfoNotificationWithAction(@NotNull MidPointClient client, String title, String content) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("midpointConnectorGenerator")
                .createNotification(title, content, NotificationType.INFORMATION);

        notification.addAction(new AnAction("Continue Development Connector Generator") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ApplicationManager.getApplication().invokeLater(() -> new ConnectorGeneratorWizard(client).show());
                notification.expire();
            }
        });

        notification.notify(client.getProject());
    }

//    private final Logger log = Logger.getInstance(this.getClass());
//
//    private final MidPointClient client;
//    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();
//
//    private JBList<NavigationItem> stepNavigationItems;
//
//    public ConnectorGeneratorWizard(@NotNull MidPointClient client) {
//        super("Connector Generator", client.getProject());
//        this.client = client;
//        buildSteps();
//        getHelpButton().setVisible(false);
//        setSize(1200, 600);
//        init();
//    }
//
//    private void buildSteps() {
//
//        Step[] steps = {
//                new WaitingBasicInfoConnectorStep(this, client, dataModel, StepStateBadge.State.NONE),
//                new BaseUrlConnectorStep(this, client, dataModel, StepStateBadge.State.NONE),
////                new SupportedAuthMethodConnectorStep(client, dataModel, StepStateBadge.State.NONE),
////                new WaitingAuthScriptsConnectorStep(client, dataModel, StepStateBadge.State.NONE),
////                new AuthScriptsConnectorStep(client, dataModel, StepStateBadge.State.NONE),
////                new CredentialsConnectorStep(client, dataModel, StepStateBadge.State.NONE),
////                new WaitingConnectivityEndpointConnectorStep(client, dataModel, StepStateBadge.State.NONE),
////                new EndpointConnectorStep(client, dataModel, StepStateBadge.State.NONE)
////                new WaitingScimSchemaConnectorStep(client, dataModel, StepStateBadge.State.NONE)
//        };
//
//        Arrays.stream(steps).forEach(this::addStep);
//        NavigationItem[] items = new NavigationItem[steps.length];
//
//        for (int i=0; i < steps.length; i++) {
//            Step step = steps[i];
//
//            step.
//
//            items[i] = new NavigationItem(step.getComponent().getName(), StepStateBadge.State.NONE, );
//        }
//
//        stepNavigationItems = new JBList<>(items);
//        stepNavigationItems.setSelectedIndex(0);
//        stepNavigationItems.setEnabled(true);
//    }
//
//    @Override
//    protected @Nullable @NonNls String getHelpID() {
//        return "";
//    }
//
//    @Override
//    protected JComponent createCenterPanel() {
//        final JBPanel<?> cellWrapperPanel = new JBPanel<>(new BorderLayout());
//        final JLabel label = new JLabel();
//        final StepStateBadge stateBadge = new StepStateBadge(StepStateBadge.State.NONE);
//
//        cellWrapperPanel.add(label, BorderLayout.CENTER);
//        cellWrapperPanel.add(stateBadge, BorderLayout.EAST);
//
//        cellWrapperPanel.setOpaque(false);
//
//        stepNavigationItems.setCellRenderer((
//                list,
//                value,
//                index,
//                isSelected,
//                cellHasFocus
//        ) -> {
//            stateBadge.setState(value.state());
//            label.setText(value.name());
//
//            if (value.isHeader()) {
//                cellWrapperPanel.setBorder(JBUI.Borders.empty(12, 10, 4, 10));
//                label.setFont(UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(Font.BOLD));
//                label.setForeground(UIUtil.getContextHelpForeground());
//                stateBadge.setVisible(false);
//            } else {
//                cellWrapperPanel.setOpaque(true);
//                cellWrapperPanel.setBorder(JBUI.Borders.empty(8, 22, 8, 10));
//                stateBadge.setVisible(true);
//
//                if (isSelected) {
//                    label.setForeground(UIUtil.getListSelectionForeground(true));
//                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
//                } else {
//                    label.setForeground(UIUtil.getLabelForeground());
//                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.PLAIN));
//                }
//            }
//            return cellWrapperPanel;
//        });
//
//        stepNavigationItems.setBorder(JBUI.Borders.empty());
//        stepNavigationItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        stepNavigationItems.setSelectionBackground(UIUtil.TRANSPARENT_COLOR);
//        stepNavigationItems.setFixedCellHeight(-1);
//
//        stepNavigationItems.addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                int index = stepNavigationItems.getSelectedIndex();
//                if (index >= 0 && index < mySteps.size()) {
//                    myCurrentStep = index;
//                    updateStep();
//                }
//            }
//        });
//
//        JBSplitter splitter = new JBSplitter(false, 0.25f);
//        splitter.setBorder(JBUI.Borders.empty());
//        splitter.setFirstComponent(stepNavigationItems);
//
//        JScrollPane mainPanel = ScrollPaneFactory.createScrollPane(super.createCenterPanel());
//        mainPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        mainPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//        mainPanel.setBorder(JBUI.Borders.empty());
//        splitter.setSecondComponent(mainPanel);
//
//        return splitter;
//    }
//
//    @Override
//    protected void doNextAction() {
//        super.doNextAction();
//        stepNavigationItems.setSelectedIndex(getCurrentStep());
//    }
//
//    @Override
//    protected void doPreviousAction() {
//        super.doPreviousAction();
//        stepNavigationItems.setSelectedIndex(getCurrentStep());
//    }
//
//    @Override
//    protected void updateStep() {
//        super.updateStep();
//        if (isLastStep()) {
//            getNextButton().setText("Finish development Connector");
//        }
//    }
}