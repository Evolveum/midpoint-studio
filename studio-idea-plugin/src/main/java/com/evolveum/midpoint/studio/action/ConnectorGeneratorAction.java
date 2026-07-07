package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.smart.api.conndev.ConnectorDevelopmentOperation;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorContinueWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

public class ConnectorGeneratorAction extends AnAction {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project is null");
            return;
        }

        var env = EnvironmentService.getInstance(project).getSelected();
        var client = new MidPointClient(project, env);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            var connection = client.testConnection();

            if (!connection.success()) {
                MidPointUtils.publishExceptionNotification(
                        project,
                        env,
                        ConnectorGeneratorAction.class,
                        "Midpoint Connection Failed",
                        connection.exception().getMessage(),
                        connection.exception()
                );
            } else {
                SearchResult searchResult = client.search(
                        ConnectorDevelopmentType.class,
                        null,
                        null
                );

                DefaultActionGroup actionGroup = new DefaultActionGroup();
                actionGroup.add(new HoverableRowAction("New Development Connector",
                        "",
                        null,
                        client));
                actionGroup.addSeparator("In-Progress Connectors");

                if (searchResult == null || searchResult.getObjects().isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() ->
                            new ConnectorGeneratorBasicWizard(client).show());
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
            }
        });
    }

    private static class HoverableRowAction extends AnAction implements CustomComponentAction {

        private final String oid;
        private final MidPointClient client;

        public HoverableRowAction(String text, String description, String oid, MidPointClient client) {
            super(text, description, null);
            this.oid = oid;
            this.client = client;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    ConnectorDevelopmentType connectorDevelopment = oid != null
                            ? client.continueFrom(oid)
                            : null;

                    ApplicationManager.getApplication().invokeLater(() ->
                            (connectorDevelopment != null
                                    ? new ConnectorGeneratorContinueWizard(client, connectorDevelopment)
                                    : new ConnectorGeneratorBasicWizard(client)
                            ).show()
                    );
                } catch (SchemaException | AuthenticationException | IOException ex) {
                    MidPointUtils.publishExceptionNotification(
                            client.getProject(),
                            client.getEnvironment(),
                            ConnectorGeneratorAction.class,
                            "Connector Development Failed",
                            ex.getMessage(),
                            ex
                    );
                }
            });
        }
    }
}
