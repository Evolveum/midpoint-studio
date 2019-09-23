package com.evolveum.midpoint.studio.impl;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.discovery.AwtRequestParameterDiscoverer;
import com.brsanthu.googleanalytics.request.DefaultRequest;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "AnalyticsManager", storages = @Storage(value = "midpoint.xml")
)
public class AnalyticsManagerImpl extends ManagerBase<AnalyticsSettings> implements AnalyticsManager, BaseComponent {

    private static final String NOTIFICATION_KEY = "MidPoint Analytics";

    private static final String DOCUMENT_HOST_NAME = "";    // TODO DOCUMENT_HOST_NAME

    private static final String TRACKING_ID = "";   // TODO TRACKING_ID

    private static final Logger LOG = Logger.getInstance(AnalyticsManagerImpl.class);

    private GoogleAnalytics analytics;

    private Map<String, Long> projectTimes = new HashMap<>();

    public AnalyticsManagerImpl() {
        super(null, AnalyticsSettings.class);
    }

    @Override
    protected AnalyticsSettings createDefaultSettings() {
        return new AnalyticsSettings();
    }

    @Override
    public void initComponent() {
        LOG.info("Analytics initialization started");

        initAnalytics();

        if (showSendUsageStatisticsNotification()) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Usage statistics", "Send usage statistics",
                    NotificationType.INFORMATION, Arrays.asList(new NotificationAction("Enable") {

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            if (analytics != null) {
                                return;
                            }

                            getSettings().setEnabled(true);
                            settingsUpdated();
                        }
                    }));
            updateVersionForUsageStatistics();
        }

        LOG.info("Analytics initialization done");
    }

    @Override
    public void disposeComponent() {
        if (analytics == null) {
            return;
        }

        try {
            sessionFinish();

            LOG.info("Closing analytics object");
            analytics.close();
        } catch (Exception ex) {
            LOG.error("Couldn't close analytics properly", ex);
        }
    }

    private boolean showSendUsageStatisticsNotification() {
        boolean enabled = getSettings().isEnabled();

        if (enabled) {
            return false;
        }

        IdeaPluginDescriptor descriptor = getPluginDescriptor();
        if (descriptor == null) {
            return false;
        }

        String version = getSettings().getPluginVersion();
        if (Objects.equals(version, descriptor.getVersion())) {
            return false;
        }

        return true;
    }

    private void updateVersionForUsageStatistics() {
        IdeaPluginDescriptor descriptor = getPluginDescriptor();
        getSettings().setPluginVersion(descriptor.getVersion());
        settingsUpdated();
    }

    private IdeaPluginDescriptor getPluginDescriptor() {
        PluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(MidPointConstants.PLUGIN_ID));
        if (pluginDescriptor != null && (pluginDescriptor instanceof IdeaPluginDescriptor)) {
            return (IdeaPluginDescriptor) pluginDescriptor;
        }

        return null;
    }

    private void initAnalytics() {
        LOG.info("Initializing analytics");

        ApplicationInfo appInfo = ApplicationInfo.getInstance();
        String ideaInfo = appInfo.getVersionName() + " " + appInfo.getFullVersion() + ", " + appInfo.getBuild().asString();

        String pluginInfo = null;
        IdeaPluginDescriptor descriptor = getPluginDescriptor();
        if (descriptor != null) {
            pluginInfo = descriptor.getName() + " " + descriptor.getReleaseVersion() + "/" + descriptor.getVersion();
        }

        analytics = GoogleAnalytics.builder()
//                .withConfig(new GoogleAnalyticsConfig().setBatchingEnabled(true).setBatchSize(10))    // todo enable batching (now working probably)
                .withDefaultRequest(new DefaultRequest()
                        .documentHostName(DOCUMENT_HOST_NAME)
                        .trackingId(TRACKING_ID)
                        .applicationName(pluginInfo)  // MidPoint 0/1.0-SNAPSHOT
                        .applicationId(ideaInfo)) // IntelliJ IDEA 2018.3, IC-183.4284.8

                .build();

        boolean enabled = getSettings().isEnabled();
        analytics.getConfig().setEnabled(enabled);

        analytics.getConfig().setValidate(true);
        analytics.getConfig().setRequestParameterDiscoverer(new AwtRequestParameterDiscoverer());

        sessionStart();
        LOG.info("Analytics initialized");
    }

    @Override
    public void sessionStart() {
        if (analytics == null) {
            return;
        }

        analytics.pageView()
                .documentPath("/")
                .sessionControl("start")
                .send();
    }

    @Override
    public void sessionFinish() {
        if (analytics == null) {
            return;
        }

        analytics.pageView()
                .documentPath("/")
                .sessionControl("end")
                .send();
    }

    @Override
    public void projectOpened(String id) {
        if (analytics == null) {
            return;
        }

        projectTimes.put(id, System.currentTimeMillis());
        analytics.screenView()
                .screenName("Project")
                .send();
    }

    @Override
    public void projectClosed(String id) {
        if (analytics == null) {
            return;
        }

        int minutes = 0;
        if (projectTimes.containsKey(id)) {
            long time = System.currentTimeMillis() - projectTimes.remove(id);
            minutes = (int) time / 60000;
        }

        analytics.timing()
                .userTimingCategory("project")
                .userTimingVariableName("time")
                .userTimingTime(minutes)
                .userTimingLabel("Time Spent")
                .send();

        analytics.flush();
    }

    /**
     * https://developers.google.com/analytics/devguides/collection/analyticsjs/events
     */
    @Override
    public void action(ActionCategory category, String id, String label, Integer value) {
        if (category == null) {
            category = ActionCategory.OTHER;
        }

        analytics.event()
                .eventCategory(category.name())
                .eventAction(id)
                .eventLabel(label)
                .eventValue(value)
                .send();
    }

    @Override
    public void screen(String id, Map<String, Object> params) {
        analytics.screenView()
                .screenName(id)
                .send();
    }

    @Override
    public void time(ActionCategory category, String id, String label, Integer value) {
        analytics.timing()
                .userTimingCategory(category.name())
                .userTimingVariableName(id)
                .userTimingTime(value)
                .userTimingLabel(label)
                .send();
    }
}
