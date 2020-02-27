package com.evolveum.midscribe.generator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorContext {

    private MidPointClient client;
    private GenerateOptions configuration;

    public GeneratorContext(GenerateOptions configuration, MidPointClient client) {
        this.configuration = configuration;
        this.client = client;
    }

    public GenerateOptions getConfiguration() {
        return configuration;
    }

    public MidPointClient getClient() {
        return client;
    }
}
