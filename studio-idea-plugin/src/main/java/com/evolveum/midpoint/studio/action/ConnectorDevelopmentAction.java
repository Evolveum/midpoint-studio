/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil;
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentContinueWizard;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.ui.Messages;

import java.awt.*;

public class ConnectorDevelopmentAction extends AnAction {

    private final static Logger log = Logger.getInstance(ConnectorDevelopmentAction.class);

    private final static String NOTIFICATION_KEY = "Connector Development Action Failed";

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project null");
            return;
        }

        var env = EnvironmentService.getInstance(project).getSelected();
        var client = new MidPointClient(project, env);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {

            var connection = client.testConnection();

            if (connection.success()) {
                try {

                    // FIXME find another way to determine whether the connector development service is enabled or disabled -> remove condition & isOfflineConnectorDevelopmentService request
                    if (client.isOfflineConnectorDevelopmentService()) {
                        return;
                    }

                    SearchResult searchResult = client.search(
                            ConnectorDevelopmentType.class,
                            null,
                            null
                    );

                    DefaultActionGroup actionGroup = new DefaultActionGroup();
                    actionGroup.add(new HoverableRowAction("New Development Connector",
                            "",
                            null,
                            client
                    ));
                    actionGroup.addSeparator("In-Progress Connectors");

                    if (searchResult == null || searchResult.getObjects().isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() ->
                                new ConnectorDevelopmentBasicWizard(client).show());
                    } else {
                        searchResult.getObjects().stream()
                                .map(dev -> new HoverableRowAction(
                                        dev.getName() != null ? dev.getName() : "Unnamed",
                                        "",
                                        dev.getOid(),
                                        client
                                ))
                                .forEach(actionGroup::add);

                        ApplicationManager.getApplication().invokeLater(() -> {
                            var dataContext = anActionEvent.getDataContext();

                            ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                                    null,
                                    actionGroup,
                                    dataContext,
                                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                    true
                            );

                            Component inputComponent = anActionEvent.getInputEvent() != null
                                    ? anActionEvent.getInputEvent().getComponent()
                                    : null;

                            if ((inputComponent != null)) {
                                popup.showUnderneathOf(inputComponent);
                            } else {
                                popup.showInBestPositionFor(dataContext);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.debug(e);
                    showNotificationException(
                            e,
                            project,
                            env
                    );
                }

            } else {
                var connectException = connection.exception();
                log.debug(connectException);
                showNotificationException(
                        connectException,
                        project,
                        env
                );
            }
        });
    }

    private static class HoverableRowAction extends AnAction {

        private final Logger log = Logger.getInstance(ConnectorDevelopmentAction.class);

        private final String oid;
        private final MidPointClient client;

        public HoverableRowAction(String text, String description, String oid, MidPointClient client) {
            super(text, description, null);
            this.oid = oid;
            this.client = client;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    ConnectorDevelopmentType connectorDevelopment =
                            oid != null ? client.continueFrom(oid) : null;

                    if (connectorDevelopment != null) {
                        ConnectorDevelopmentWizardUtil.downloadConnectorDevelopment(
                                client,
                                connectorDevelopment,
                                () -> ApplicationManager.getApplication().invokeLater(() ->
                                        new ConnectorDevelopmentContinueWizard(
                                                client,
                                                connectorDevelopment
                                        ).show()
                                )
                        );
                    } else {
                        ApplicationManager.getApplication().invokeLater(() ->
                                new ConnectorDevelopmentBasicWizard(client).show()
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to continue connector development", e);

                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showErrorDialog(
                                    client.getProject(),
                                    e.getMessage(),
                                    "Error Connector Development Continue"
                            )
                    );
                }
            });
        }
    }

    private static void showNotificationException(
            Exception e,
            Project project,
            Environment env
    ) {
        MidPointUtils.publishExceptionNotification(
                project,
                env,
                ConnectorDevelopmentAction.class,
                NOTIFICATION_KEY,
                e.getMessage(),
                e
        );
    }
}
