package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.client.api.Service;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorContext {

    private Service client;
    private GenerateOptions configuration;

    public GeneratorContext(GenerateOptions configuration, Service client) {
        this.configuration = configuration;
        this.client = client;
    }

    public GenerateOptions getConfiguration() {
        return configuration;
    }

    public Service getClient() {
        return client;
    }
}
