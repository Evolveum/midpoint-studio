package com.evolveum.midscribe.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    private static final Map<ExportFormat, Class<? extends Exporter>> EXPORTERS;

    static {
        Map<ExportFormat, Class<? extends Exporter>> exporters = new HashMap<>();
        exporters.put(ExportFormat.PDF, PdfExporter.class);
        exporters.put(ExportFormat.HTML, HtmlExporter.class);

        EXPORTERS = Collections.unmodifiableMap(exporters);
    }

    private GenerateOptions configuration;

    private LogListener logListener;

    public Generator(GenerateOptions configuration) {
        this.configuration = configuration;
    }

    public LogListener getLogListener() {
        return logListener;
    }

    public void setLogListener(LogListener logListener) {
        this.logListener = logListener;
    }

    public void generate() throws Exception {
        generate(new Properties());
    }

    public void generate(Properties properties) throws Exception {
        MidPointObjectStore store = createObjectStore();

        File adocFile = createAdocFile();

        try (Writer output = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(adocFile), StandardCharsets.UTF_8))) {

            VelocityGeneratorProcessor processor = new VelocityGeneratorProcessor(configuration, properties);

            GeneratorContext ctx = new GeneratorContext(configuration, store);
            processor.process(output, ctx);
        }

        LOG.info("Asciidoc file '{}' generated for all objects", adocFile.getPath());

        if (configuration.getExportFormat() == null) {
            return;
        }

        Exporter exporter = createExporter(configuration.getExportFormat());
        if (exporter == null) {
            LOG.info("No exporter defined, finishing");
            return;
        }

        File exportFile = createExportFile(exporter);
        LOG.debug("Preparing export from adoc {} to {}", adocFile, exportFile);

        exporter.export(adocFile, exportFile);
    }

    private Exporter createExporter(ExportFormat format) {
        Class<? extends Exporter> clazz = EXPORTERS.get(format);
        if (clazz == null) {
            return null;
        }

        try {
            Exporter exporter = clazz.getDeclaredConstructor().newInstance();
            exporter.setLogListener(logListener);

            return exporter;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            LOG.error("Couldn't create formatter of type " + clazz);
            return null;
        }
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

    private MidPointObjectStore createObjectStore() throws Exception {
        MidPointObjectStore store = configuration.getObjectStoreInstance();
        if (store != null) {
            LOG.debug("Using midPoint store instance: {}" + store.getClass().getName());
            return store;
        }

        Class<? extends MidPointObjectStore> storeType = configuration.getObjectStoreType();
        if (storeType == null) {
            storeType = InMemoryObjectStore.class;
        }

        LOG.debug("Setting up midPoint store from class: {}", storeType);

        Constructor<? extends MidPointObjectStore> con = storeType.getConstructor(GenerateOptions.class);
        store = con.newInstance(configuration);
        store.init();

        return store;
    }
}
