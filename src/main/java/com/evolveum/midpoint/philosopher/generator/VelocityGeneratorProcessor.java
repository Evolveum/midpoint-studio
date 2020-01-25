package com.evolveum.midpoint.philosopher.generator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Created by Viliam Repan (lazyman).
 */
public class VelocityGeneratorProcessor {

    public VelocityGeneratorProcessor() {
        init();
    }

    private void init() {
        Properties props = new Properties();
        props.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
        props.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        Velocity.init(props);
    }

    private Template getTemplate(String name) {
        return Velocity.getTemplate(name, StandardCharsets.UTF_8.name());
    }

    public void process(Writer output, GeneratorContext ctx) throws IOException {
        Template template = getTemplate("/template/documentation.vm");
        if (template == null) {
            return;
        }

        VelocityContext context = new VelocityContext();
        context.put("configuration", ctx.getConfiguration());
        context.put("client", ctx.getClient());
        context.put("utils", TemplateUtils.class);
        context.put("processor", new ProcessorUtils(ctx));

        template.merge(context, output);
    }
}
