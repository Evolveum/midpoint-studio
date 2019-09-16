package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.prism.PrismContext;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceContext {

    private PrismContext prismContext;

    private WebClient client;

    public ServiceContext(PrismContext prismContext, WebClient client) {
        this.prismContext = prismContext;
        this.client = client;
    }

    public PrismContext getPrismContext() {
        return prismContext;
    }

    public WebClient getClient() {
        return client;
    }
}
