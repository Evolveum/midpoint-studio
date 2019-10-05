package com.evolveum.midpoint.studio.impl;

import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EmptyAnalyticsManager implements AnalyticsManager {

    @Override
    public void sessionStart() {

    }

    @Override
    public void sessionFinish() {

    }

    @Override
    public void projectOpened(String id) {

    }

    @Override
    public void projectClosed(String id) {

    }

    @Override
    public void action(ActionCategory category, String id, String label, Integer value) {

    }

    @Override
    public void screen(String id, Map<String, Object> params) {

    }

    @Override
    public void time(ActionCategory category, String id, String label, Integer value) {

    }
}
