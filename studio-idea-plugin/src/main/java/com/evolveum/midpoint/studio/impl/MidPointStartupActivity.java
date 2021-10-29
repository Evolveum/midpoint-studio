package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ToolsImpl;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.notification.BrowseNotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(MidPointStartupActivity.class);

    private static final String[] IGNORED_RESOURCES = {
            "http://midpoint.evolveum.com/xml/ns/public/common/org-3",
            "http://prism.evolveum.com/xml/ns/public/matching-rule-3",
            "http://midpoint.evolveum.com/xml/ns/public/resource/instance-3"
    };

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Initializing service factory");
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        LOG.info("Service factory initialized " + ctx.toString());

        ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
        String[] ignored = manager.getIgnoredResources();

        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(ignored));

        List<String> toAdd = new ArrayList<>();

        for (String s : IGNORED_RESOURCES) {
            if (set.contains(s)) {
                continue;
            }

            toAdd.add(s);
        }

        manager.addIgnoredResources(toAdd, null);

        ProjectInspectionProfileManager pipManager = ProjectInspectionProfileManager.getInstance(project);
        InspectionProfileImpl profile = pipManager.getCurrentProfile();
        ToolsImpl tool = profile.getToolsOrNull("CheckValidXmlInScriptTagBody", project);
        if (tool != null) {
            tool.setEnabled(false);
        }

        showVersioningChangeNotification(project);
    }

    private void showVersioningChangeNotification(Project project) {
        IdeaPluginDescriptor descriptor = PluginManager.getInstance().findEnabledPlugin(PluginId.getId(MidPointConstants.PLUGIN_ID));
        if (descriptor == null) {
            return;
        }

        String version = descriptor.getVersion();
        if (version.matches("\\d\\.\\d\\.\\d.*")) {
            return;
        }

        MidPointUtils.publishNotification(project, "VERSION_CHANGE", "Release cycle changes",
                "Release cycle for MidPoint Studio plugin will change with release 4.4. " +
                        "There will be only two channels snapshot and stable (default).\n" +
                        "If you're using nightly/milestone channel, please uninstall MidPoint Studio plugin, switch to 'snapshot' channel and install it again. " +
                        "You will not loose any project environment configurations.",
                NotificationType.INFORMATION,
                new BrowseNotificationAction("More info...", "https://docs.evolveum.com/midpoint/tools/studio/builds/"));
    }
}
