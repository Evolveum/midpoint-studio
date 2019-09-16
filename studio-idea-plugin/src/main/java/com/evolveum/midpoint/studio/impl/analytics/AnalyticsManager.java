package com.evolveum.midpoint.studio.impl.analytics;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.discovery.AwtRequestParameterDiscoverer;
import com.brsanthu.googleanalytics.request.DefaultRequest;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "AnalyticsManager", storages = @Storage(value = "midpoint.xml")
)
public class AnalyticsManager implements BaseComponent, PersistentStateComponent<AnalyticsSettings> {

    private AnalyticsSettings settings;

    private GoogleAnalytics analytics;

    public static AnalyticsManager getInstance() {
        Application application = ApplicationManager.getApplication();
        return application.getComponent(AnalyticsManager.class);
    }

    @Nullable
    @Override
    public AnalyticsSettings getState() {
        return settings;
    }

    @Override
    public void loadState(@NotNull AnalyticsSettings state) {
        settings = state;
    }

    @Override
    public void noStateLoaded() {
        settings = new AnalyticsSettings();
    }

    public static GoogleAnalytics getAnalytics() {
        return getInstance().getGoogleAnalytics();
    }

    public static void sendPageView(Object... params) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String[] type = element.getClassName().split("\\.");

        sendPageView("/" + type[type.length - 1] + "/" + element.getMethodName(), params);
    }

    public static void sendPageView(String documentPath, Object[] params) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < params.length; i++) {
            if (i + 1 >= params.length) {
                break;
            }
            list.add(params[i] + "=" + params[++i]);
        }

        if (!list.isEmpty()) {
            documentPath += "?" + StringUtils.join(list, "&");
        }
        getAnalytics().pageView().documentPath(documentPath).send();
    }

    @Override
    public void initComponent() {
        String ip = "127.0.0.1";
        try {
            ip = Ipify.getPublicIp();   // todo move to different thread
        } catch (Exception ex) {
            ex.printStackTrace();   // todo log exception
        }

        ApplicationInfo appInfo = ApplicationInfo.getInstance();
        String ideaInfo = appInfo.getVersionName() + " " + appInfo.getFullVersion() + ", " + appInfo.getBuild().asString();

        PluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(MidPointConstants.PLUGIN_ID));
        String pluginInfo = null;
        if (pluginDescriptor != null && (pluginDescriptor instanceof IdeaPluginDescriptor)) {
            IdeaPluginDescriptor pDescriptor = (IdeaPluginDescriptor) pluginDescriptor;
            pluginInfo = pDescriptor.getName() + " " + pDescriptor.getReleaseVersion() + "/" + pDescriptor.getVersion();
        }

        analytics = GoogleAnalytics.builder()
//                .withConfig(new GoogleAnalyticsConfig().setBatchingEnabled(true).setBatchSize(10))    // todo enable batching (now working probably)
                .withDefaultRequest(new DefaultRequest()
                        .documentHostName("")   // todo
                        .userIp(ip)
                        .trackingId("") // todo
                        .applicationName(pluginInfo)  // MidPoint 0/1.0-SNAPSHOT
                        .applicationId(ideaInfo)) // IntelliJ IDEA 2018.3, IC-183.4284.85
                .build();

        analytics.getConfig().setValidate(true);
        analytics.getConfig().setRequestParameterDiscoverer(new AwtRequestParameterDiscoverer());

        // todo analytics fail when internet is not available?

        analytics.getConfig().setEnabled(false); // todo remove

        analytics.pageView()
                .documentPath("/")
                .sessionControl("start")
                .send();
    }

    @Override
    public void disposeComponent() {
        analytics.pageView()
                .documentPath("/")
                .sessionControl("end")
                .send();

        try {
            analytics.close();
        } catch (Exception ex) {
            ex.printStackTrace(); // todo log exception
        }
    }

    public GoogleAnalytics getGoogleAnalytics() {
        return analytics;
    }

    public void enableAnalytics() {
        getAnalytics().getConfig().setEnabled(true);
    }

    public void disableAnalytics() {
        getAnalytics().getConfig().setEnabled(false);
    }
}
