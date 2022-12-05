package com.evolveum.midscribe.generator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorContext {

    private MidPointObjectStore store;
    private GenerateOptions configuration;

    public GeneratorContext(GenerateOptions configuration, MidPointObjectStore store) {
        this.configuration = configuration;
        this.store = store;
    }

    public GenerateOptions getConfiguration() {
        return configuration;
    }

    public MidPointObjectStore getStore() {
        return store;
    }
}
