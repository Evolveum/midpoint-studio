package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.action.ShowConfigurationAction;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.ide.MavenManagerListener;
import com.evolveum.midpoint.studio.impl.lang.codeInsight.NonexistentNamespaceUriCompletionProvider;
import com.evolveum.midpoint.studio.ui.configuration.CredentialsConfigurable;
import com.evolveum.midpoint.studio.ui.configuration.EnvironmentsConfigurable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ToolsImpl;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.project.ProjectKt;
import com.intellij.ui.NewUI;
import com.intellij.util.ModalityUiUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointStartupActivity implements ProjectActivity {

    private static final Logger LOG = Logger.getInstance(MidPointStartupActivity.class);

    public static String TITLE = "MidPoint Studio Facet";

    public static String NOTIFICATION_KEY = TITLE;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (MidPointUtils.hasMidPointFacet(project)) {
            // listen for maven project refresh event (after import) and check whether facet is there
            MavenProjectsManager mpm = MavenProjectsManager.getInstance(project);
            mpm.addManagerListener(new MavenManagerListener(project));
        }

        initializePrism();

        RunnableUtils.invokeLaterIfNeeded(() -> RunnableUtils.runWriteAction(() -> initializeIgnoredResources()));

        initializeInspections(project);

        RunnableUtils.executeOnPooledThread(() -> validateStudioConfiguration(project));

        RunnableUtils.invokeLaterIfNeeded(() -> initializeUI());

        return null;
    }

    private void initializePrism() {
        LOG.info("Initializing service factory");
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        LOG.info("Service factory initialized " + ctx.toString());
    }

    private void initializeIgnoredResources() {
        ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
        String[] ignored = manager.getIgnoredResources();

        Set<String> set = new HashSet<>(Arrays.asList(ignored));

        List<String> toAdd = new ArrayList<>();

        for (String s : NonexistentNamespaceUriCompletionProvider.IGNORED_RESOURCES) {
            if (set.contains(s)) {
                continue;
            }

            toAdd.add(s);
        }

        manager.addIgnoredResources(toAdd, null);
    }

    private void initializeInspections(Project project) {
        ProjectInspectionProfileManager pipManager = ProjectInspectionProfileManager.getInstance(project);
        InspectionProfileImpl profile = pipManager.getCurrentProfile();
        ToolsImpl tool = profile.getToolsOrNull("CheckValidXmlInScriptTagBody", project);
        if (tool != null) {
            tool.setEnabled(false);
        }
    }

    private void initializeUI() {
        if (!NewUI.isEnabled()) {
            return;
        }

        ActionManager am = ActionManager.getInstance();
        AnAction action = am.getAction("MidPoint.ExpUI.Toolbar.Main");
        DefaultActionGroup parent = (DefaultActionGroup) am.getAction("MainToolbarRight");
        if (parent == null) {
            parent = (DefaultActionGroup) am.getAction("RunToolbarWidgetCustomizableActionGroup");
        }

        if (!parent.containsAction(action)) {
            parent.add(action, Constraints.FIRST);
        }
    }

    private void validateStudioConfiguration(Project project) {
        IProjectStore store = ProjectKt.getStateStore(project);
        Path path = store.getDirectoryStorePath();
        if (path == null) {
            return;
        }

        File midpoint = new File(path.toFile(), "midpoint.xml");
        if (!midpoint.exists()) {
            return;
        }

        ModuleManager mm = ModuleManager.getInstance(project);
        Module[] modules = mm.getModules();
        if (modules.length == 0) {
            return;
        }

        Module moduleWithFacet = null;
        for (Module module : modules) {
            FacetManager fm = FacetManager.getInstance(module);
            if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) != null) {
                moduleWithFacet = module;
                break;
            }
        }

        if (moduleWithFacet != null) {
            validateCredentialsConfiguration(moduleWithFacet);
            return;
        }

        // we would like to add midpoint studio facet

        MidPointService ms = MidPointService.get(project);
        boolean ask = ms.getSettings().isAskToAddMidpointFacet();
        if (!ask) {
            return;
        }

        Module module = modules[0];

        ModalityUiUtil.invokeLaterIfNeeded(
                ModalityState.nonModal(), () -> {
                    MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                            StudioLocalization.message("MidPointStartupActivity.checkFacet.title"),
                            StudioLocalization.message("MidPointStartupActivity.checkFacet.msg", module.getName()), // todo this ignores parameter???
                            NotificationType.INFORMATION,
                            NotificationAction.createExpiring(StudioLocalization.message("MidPointStartupActivity.checkFacet.addFacet"), (evt, notification) -> addFacetPerformed(module)),
                            NotificationAction.createExpiring(StudioLocalization.message("MidPointStartupActivity.checkFacet.dontAsk"), (evt, notification) -> dontAskAboutMidpointConfigurationAgainPerformed(project)));
                });
    }

    private void addFacetPerformed(Module module) {
        FacetManager fm = FacetManager.getInstance(module);
        if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) != null) {
            validateCredentialsConfiguration(module);
            return;
        }

        RunnableUtils.runWriteAction(() -> {
            FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
            FacetUtil.addFacet(module, facetType, facetType.getPresentableName());

            validateCredentialsConfiguration(module);
        });
    }

    private void dontAskAboutMidpointConfigurationAgainPerformed(Project project) {
        MidPointService ms = MidPointService.get(project);
        ms.getSettings().setAskToAddMidpointFacet(false);
        ms.settingsUpdated();
    }

    private void validateCredentialsConfiguration(Module module) {
        Project project = module.getProject();

        MidPointService ms = MidPointService.get(project);
        boolean ask = ms.getSettings().isAskToValidateEnvironmentCredentials();
        if (!ask) {
            return;
        }

        // todo check whether there is credentials.kdbx and master password is available and valid
        validateProjectCredentialsConfiguration(project);

        validateEnvironmentsConfiguration(module);
    }

    private void validateProjectCredentialsConfiguration(Project project) {
        EncryptionService service = EncryptionService.getInstance(project);
        EncryptionService.StatusMessage status = service.getStatus();

        if (status.getStatus() == EncryptionService.Status.OK) {
            return;
        }

        MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                StudioLocalization.message("MidPointStartupActivity.checkProjectCredentials.title"),
                StudioLocalization.message(
                        "MidPointStartupActivity.checkProjectCredentials.msg", status.getMessage()),
                NotificationType.INFORMATION,
                NotificationAction.createExpiring(
                        StudioLocalization.message("MidPointStartupActivity.checkProjectCredentials.openConfiguration"),
                        (evt, notification) -> new ShowConfigurationAction(CredentialsConfigurable.class).actionPerformed(evt)),
                NotificationAction.createExpiring(
                        StudioLocalization.message("MidPointStartupActivity.checkProjectCredentials.dontAsk"),
                        (evt, notification) -> dontAskAboutCredentialsAgainPerformed(project)));
    }

    private void validateEnvironmentsConfiguration(Module module) {
        Project project = module.getProject();

        boolean check = false;
        EnvironmentService es = EnvironmentService.getInstance(project);
        List<Environment> environments = es.getEnvironments();
        for (Environment env : environments) {
            if (env.getUsername() == null || env.getPassword() == null) {
                check = true;
                break;
            }
        }

        if (!check) {
            return;
        }

        MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                StudioLocalization.message("MidPointStartupActivity.checkEnvironmentCredentials.title"),
                StudioLocalization.message("MidPointStartupActivity.checkEnvironmentCredentials.msg", module.getName()),
                NotificationType.INFORMATION,
                NotificationAction.createExpiring(
                        StudioLocalization.message("MidPointStartupActivity.checkCredentials.openConfiguration"),
                        (evt, notification) -> new ShowConfigurationAction(EnvironmentsConfigurable.class).actionPerformed(evt)),
                NotificationAction.createExpiring(
                        StudioLocalization.message("MidPointStartupActivity.checkCredentials.dontAsk"),
                        (evt, notification) -> dontAskAboutCredentialsAgainPerformed(project)));
    }

    private void dontAskAboutCredentialsAgainPerformed(Project project) {
        MidPointService ms = MidPointService.get(project);
        ms.getSettings().setAskToValidateEnvironmentCredentials(false);
        ms.settingsUpdated();
    }
}
