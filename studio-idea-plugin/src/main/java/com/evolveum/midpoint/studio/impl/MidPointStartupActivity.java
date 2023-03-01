package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.impl.lang.codeInsight.NonexistentNamespaceUriCompletionProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ToolsImpl;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.ui.ExperimentalUI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(MidPointStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        initializePrism();

        initializeIgnoredResources();

        initializeInspections(project);

        initializeUI();

        validateStudioConfiguration(project);
    }

    private void initializePrism() {
        LOG.info("Initializing service factory");
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        LOG.info("Service factory initialized " + ctx.toString());
    }

    private void initializeIgnoredResources() {
        ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
        String[] ignored = manager.getIgnoredResources();

        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(ignored));

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
        if (!ExperimentalUI.isNewUI()) {
            return;
        }

        ActionManager am = ActionManager.getInstance();
        AnAction action = am.getAction("MidPoint.ExpUI.Toolbar.Main");
        DefaultActionGroup parent = (DefaultActionGroup) am.getAction("MainToolbarRight");
        if (parent == null) {
            parent = (DefaultActionGroup) am.getAction("RunToolbarWidgetCustomizableActionGroup");
        }
        parent.add(action, Constraints.FIRST);
    }

    private void validateStudioConfiguration(Project project) {
        // todo implement validation of project facet if ./idea/midpoint.xml exist and if it doesn't contain some boolean "dontCheckFacetDuringStartupWhatewer"
    }
}
