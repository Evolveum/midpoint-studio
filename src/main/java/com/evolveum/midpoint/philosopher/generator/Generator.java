package com.evolveum.midpoint.philosopher.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public Generator(GenerateOptions configuration) {
        this.configuration = configuration;
    }

    public void generate() throws Exception {
        MidPointClient client = new LocalClient(configuration.getLocal());
        client.init();

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
}
