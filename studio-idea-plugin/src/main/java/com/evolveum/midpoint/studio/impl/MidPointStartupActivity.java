package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.action.ShowEnvironmentConfigurationAction;
import com.evolveum.midpoint.studio.impl.ide.MavenManagerListener;
import com.evolveum.midpoint.studio.impl.lang.codeInsight.NonexistentNamespaceUriCompletionProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.studio.util.StudioBundle;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ToolsImpl;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.project.ProjectKt;
import com.intellij.ui.NewUI;
import com.intellij.util.ModalityUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(MidPointStartupActivity.class);

    public static String TITLE = "MidPoint Studio Facet";

    public static String NOTIFICATION_KEY = TITLE;

    @Override
    public void runActivity(@NotNull Project project) {
        if (MidPointUtils.hasMidPointFacet(project)) {
            // listen for maven project refresh event (after import) and check whether facet is there
            MavenProjectsManager mpm = MavenProjectsManager.getInstance(project);
            mpm.addManagerListener(new MavenManagerListener(project));
        }

        initializePrism();

        initializeIgnoredResources();

        initializeInspections(project);

        initializeUI();

        ModalityUiUtil.invokeLaterIfNeeded(ModalityState.any(), () -> validateStudioConfiguration(project));
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

        MidPointService ms = MidPointService.getInstance(project);
        boolean ask = ms.getSettings().isAskToAddMidpointFacet();
        if (!ask) {
            return;
        }

        Module module = modules[0];

        MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                StudioBundle.message("MidPointStartupActivity.checkFacet.title"),
                StudioBundle.message("MidPointStartupActivity.checkFacet.msg", module.getName()),
                NotificationType.INFORMATION,
                NotificationAction.createExpiring(StudioBundle.message("MidPointStartupActivity.checkFacet.addFacet"), (evt, notification) -> addFacetPerformed(module)),
                NotificationAction.createExpiring(StudioBundle.message("MidPointStartupActivity.checkFacet.dontAsk"), (evt, notification) -> dontAskAboutMidpointConfigurationAgainPerformed(project)));
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
        MidPointService ms = MidPointService.getInstance(project);
        ms.getSettings().setAskToAddMidpointFacet(false);
        ms.settingsUpdated();
    }

    private void validateCredentialsConfiguration(Module module) {
        Project project = module.getProject();

        MidPointService ms = MidPointService.getInstance(project);
        boolean ask = ms.getSettings().isAskToValidateEnvironmentCredentials();
        if (!ask) {
            return;
        }

        // todo check whether there is credentials.kdbx and master password is available and valid

        validateEnvironmentsConfiguration(module);
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
                StudioBundle.message("MidPointStartupActivity.checkCredentials.title"),
                StudioBundle.message("MidPointStartupActivity.checkCredentials.msg", module.getName()),
                NotificationType.INFORMATION,
                NotificationAction.createExpiring(StudioBundle.message("MidPointStartupActivity.checkCredentials.openConfiguration"), (evt, notification) -> openEnvironmentsConfiguration(evt)),
                NotificationAction.createExpiring(StudioBundle.message("MidPointStartupActivity.checkCredentials.dontAsk"), (evt, notification) -> dontAskAboutCredentialsAgainPerformed(project)));
    }

    private void openEnvironmentsConfiguration(AnActionEvent evt) {
        new ShowEnvironmentConfigurationAction().actionPerformed(evt);
    }

    private void dontAskAboutCredentialsAgainPerformed(Project project) {
        MidPointService ms = MidPointService.getInstance(project);
        ms.getSettings().setAskToValidateEnvironmentCredentials(false);
        ms.settingsUpdated();
    }
}
