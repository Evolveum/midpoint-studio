package com.evolveum.midpoint.studio.cmd.executor.configuration;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Midpoint {

    private List<String> urls;

    private MidpointProperties properties;

    private MidpointUser user;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public MidpointProperties getProperties() {
        return properties;
    }

    public void setProperties(MidpointProperties properties) {
        this.properties = properties;
    }

    public MidpointUser getUser() {
        return user;
    }

    public void setUser(MidpointUser user) {
        this.user = user;
    }
}
