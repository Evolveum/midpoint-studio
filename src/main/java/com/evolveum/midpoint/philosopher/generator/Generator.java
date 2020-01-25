package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Generator {

    private static final Logger LOG = LoggerFactory.getLogger(Generator.class);

    private static final String ADOC_EXTENSION = ".adoc";

    private static final Map<ExportFormat, Exporter> EXPORTERS;

    static {
        Map<ExportFormat, Exporter> exporters = new HashMap<>();
        exporters.put(ExportFormat.PDF, new PdfExporter());
        exporters.put(ExportFormat.HTML, new HtmlExporter());

        EXPORTERS = Collections.unmodifiableMap(exporters);
    }

    private GenerateOptions configuration;

    private Service client;

    public Generator(GenerateOptions configuration, Service client) {
        this.configuration = configuration;
        this.client = client;
    }

    public void generate() throws Exception {
        Map<Class<? extends ObjectType>, Set<String>> types = createDefaultTypes();

        File adocFile = createAsciidocFile();
        if (adocFile.exists()) {
            adocFile.delete();
        }

        adocFile.createNewFile();

        try (Writer output = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(adocFile), StandardCharsets.UTF_8))) {

            VelocityGeneratorProcessor processor = new VelocityGeneratorProcessor();

            GeneratorContext ctx = new GeneratorContext(configuration, client);
            processor.process(output, ctx);
        } catch (IOException ex) {
            // todo error handling
            ex.printStackTrace();
        }

        LOG.info("Asciidoc file '{}' generated for all objects", adocFile.getPath());

        if (configuration.getExportFormat() == null) {
            return;
        }

        Exporter exporter = EXPORTERS.get(configuration.getExportFormat());
        if (exporter == null) {
            LOG.info("No exporter defined, finishing");
            return;
        }

        File output = configuration.getOutput();
        if (output.exists()) {
            output.delete();
        }

        try {
            output.createNewFile();

            exporter.export(adocFile, output);
        } catch (IOException ex) {
            // todo error handling
            ex.printStackTrace();
        }
    }

    private File createAsciidocFile() {
        File output = configuration.getOutput();
        if (configuration.getExportFormat() == null) {
            return output;
        }

        return new File(output.getParent(), output.getName() + ADOC_EXTENSION);
    }

    private Map<Class<? extends ObjectType>, Set<String>> createDefaultTypes() {
        Map<Class<? extends ObjectType>, Set<String>> map = new HashMap<>();
        map.put(ResourceType.class, new HashSet<>());
        map.put(ObjectTemplateType.class, new HashSet<>());
        map.put(FunctionLibraryType.class, new HashSet<>());
        map.put(ConnectorType.class, new HashSet<>());

        // todo figure out which types should be used by default

        return map;
    }
}
