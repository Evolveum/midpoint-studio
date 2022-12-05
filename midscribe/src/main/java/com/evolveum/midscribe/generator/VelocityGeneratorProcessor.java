package com.evolveum.midscribe.generator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import static com.evolveum.midscribe.generator.GeneratorProperties.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class VelocityGeneratorProcessor {

    private GenerateOptions options;

    private VelocityEngine engine;

    private String velocityStartTemplate;

    private Map<String, Object> velocityAdditionalVariables;

    public VelocityGeneratorProcessor(GenerateOptions options, Properties props) {
        this.options = options;

        init(props);
    }

    private void init(Properties properties) {
        velocityStartTemplate = getProperty(properties, VELOCITY_START_TEMPLATE, String.class, VELOCITY_START_TEMPLATE_DEFAULT);
        velocityAdditionalVariables = getProperty(properties, VELOCITY_ADDITIONAL_VARIABLES, Map.class, VELOCITY_ADDITIONAL_VARIABLES_DEFAULT);

        File template = options.getTemplate();

        Properties props = new Properties();
        props.put(RuntimeConstants.RESOURCE_LOADER, "composite");
        props.put("composite.resource.loader.instance", new CompositeResourceLoader(template));

        if (properties != null) {
            props.putAll(properties);
        }

        engine = new VelocityEngine();
        engine.init(props);
    }

    private <T> T getProperty(Properties properties, String name, Class<T> type, T defaultValue) {
        Object value = properties.get(name);
        if (value == null) {
            return defaultValue;
        }

        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Property value for " + name + " has wrong type " + value.getClass() + ", should be " + type);
        }

        return (T) value;
    }

    private Template getTemplate(String name) {
        return engine.getTemplate(name, StandardCharsets.UTF_8.name());
    }

    public void process(Writer output, GeneratorContext ctx) throws Exception {
        Template template = getTemplate(velocityStartTemplate);
        if (template == null) {
            return;
        }

        VelocityContext context = new VelocityContext();
        context.put("configuration", ctx.getConfiguration());
        context.put("store", ctx.getStore());
        context.put("utils", TemplateUtils.class);
        context.put("processor", new ProcessorUtils(ctx));

        if (velocityAdditionalVariables != null) {
            velocityAdditionalVariables.forEach((k, v) -> context.put(k, v));
        }

        TemplateEngineContextBuilder builder = createTemplateEngineContextBuilder();
        if (builder != null) {
            builder.registerVariables(context, ctx);
        }

        template.merge(context, output);
    }

    private TemplateEngineContextBuilder createTemplateEngineContextBuilder() throws Exception {
        Class<? extends TemplateEngineContextBuilder> type = options.getTemplateEngineContextBuilder();
        if (type == null) {
            return null;
        }

        return type.getDeclaredConstructor().newInstance();
    }
}
