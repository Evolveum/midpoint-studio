package com.evolveum.midpoint.studio.impl;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.evolveum.midpoint.studio.impl.analytics.AnalyticsManager;
import com.evolveum.midpoint.studio.ui.MidPointConsoleView;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "MidPointManager", storages = @Storage(value = "midpoint.xml")
)
public class MidPointManager extends ManagerBase<MidPointSettings> implements ProjectComponent {

    private MidPointConsoleView console;

    public MidPointManager(@NotNull Project project) {
        super(project, MidPointSettings.class);
    }

    @Override
    protected MidPointSettings createDefaultSettings() {
        return MidPointSettings.createDefaultSettings();
    }

    @Override
    public void projectOpened() {
        GoogleAnalytics analytics = AnalyticsManager.getAnalytics();
        analytics.pageView()
                .documentPath("/project")
                .send();
    }

    @Override
    public void projectClosed() {
        GoogleAnalytics analytics = AnalyticsManager.getAnalytics();
        analytics.flush();
    }

    public static MidPointManager getInstance(@NotNull Project project) {
        return project.getComponent(MidPointManager.class);
    }

    public void setConsole(MidPointConsoleView console) {
        this.console = console;
    }

    public MidPointConsoleView getConsole() {
        return console;
    }
}
