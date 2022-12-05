package com.evolveum.midscribe.generator;

import org.apache.velocity.VelocityContext;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface TemplateEngineContextBuilder {

    void registerVariables(VelocityContext ctx, GeneratorContext generatorContext);
}
