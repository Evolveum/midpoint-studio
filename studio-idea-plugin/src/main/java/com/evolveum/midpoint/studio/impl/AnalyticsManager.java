package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface AnalyticsManager {

    static AnalyticsManager getInstance() {
        Application application = ApplicationManager.getApplication();
        return application.getComponent(AnalyticsManager.class);
    }

    void sessionStart();

    void sessionFinish();

    void projectOpened(String id);

    void projectClosed(String id);

    void action(ActionCategory category, String id, String label, Integer value);

    void screen(String id, Map<String, Object> params);
}
