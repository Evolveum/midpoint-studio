package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GetObjectOptions {

    enum Source {

        LOCAL, CACHE, REMOTE
    }

    private static final Source[] DEFAULT_SOURCES = {
            Source.LOCAL,
            Source.CACHE
    };

    private Source[] sources = DEFAULT_SOURCES;

    private boolean fetchAlways;

    public Source[] getSources() {
        return sources;
    }

    public void setSources(Source[] sources) {
        this.sources = sources;
    }

    public boolean isFetchAlways() {
        return fetchAlways;
    }

    public void setFetchAlways(boolean fetchAlways) {
        this.fetchAlways = fetchAlways;
    }
}
