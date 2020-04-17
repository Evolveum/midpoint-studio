package com.evolveum.midscribe.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        generate(new Properties());
    }

    public void generate(Properties properties) throws Exception {
        Class<? extends MidPointClient> clientType = configuration.getMidpointClient();
        if (clientType == null) {
            clientType = InMemoryClient.class;
        }
        MidPointClient client = createMidPointClient(clientType);

        File adocFile = createAdocFile();

        try (Writer output = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(adocFile), StandardCharsets.UTF_8))) {

            VelocityGeneratorProcessor processor = new VelocityGeneratorProcessor(configuration.getTemplate(), properties);

            GeneratorContext ctx = new GeneratorContext(configuration, client);
            processor.process(output, ctx);
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

        File exportFile = createExportFile(exporter);
        LOG.debug("Preparing export from adoc {} to {}", adocFile, exportFile);

        exporter.export(adocFile, exportFile);
    }

    private File createExportFile(Exporter exporter) throws IOException {
        File adocOutput = configuration.getAdocOutput();
        File exportOutput = configuration.getExportOutput();

        if (exportOutput == null) {
            exportOutput = new File(adocOutput.getParent(), adocOutput.getName() + "." + exporter.getDefaultExtension());
        }

        return createFile(exportOutput);
    }

    private File createAdocFile() throws IOException {
        File adocOutput = configuration.getAdocOutput();
        File exportOutput = configuration.getExportOutput();

        if (adocOutput == null) {
            adocOutput = new File(exportOutput.getParent(), exportOutput.getName() + ADOC_EXTENSION);
        }

        return createFile(adocOutput);
    }

    private File createFile(File file) throws IOException {
        LOG.debug("Creating file " + file.getAbsolutePath());

        if (file.exists()) {
            file.delete();
        }

        file.createNewFile();

        return file;
    }

    private MidPointClient createMidPointClient(Class<? extends MidPointClient> type) throws Exception {
        Constructor<? extends MidPointClient> con = type.getConstructor(GenerateOptions.class);
        MidPointClient client = con.newInstance(configuration);
        client.init();

        return client;
    }
}
