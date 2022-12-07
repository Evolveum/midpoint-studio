package com.evolveum.midscribe;

import com.evolveum.midscribe.generator.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorTest extends MidscribeTest {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorTest.class);

    @Test
    public void generateExample() throws Exception {
        GenerateOptions opts = prepareOptions("generateExample");
        opts.setSourceDirectory(List.of(new File("./src/test/resources/objects")));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generatePdfExample() throws Exception {
        GenerateOptions opts = prepareOptions("generatePdfExample");
        opts.setSourceDirectory(List.of(new File("./src/test/resources/objects")));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setExportFormat(ExportFormat.PDF);

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateWithCustomZipTemplate() throws Exception {
        GenerateOptions opts = prepareOptions("generateWithCustomZipTemplate");
        opts.setSourceDirectory(List.of(new File("./src/test/resources/objects")));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setTemplate(new File("./src/test/resources/template.zip"));

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateWithCustomDirectoryTemplate() throws Exception {
        GenerateOptions opts = prepareOptions("generateWithCustomDirectoryTemplate");
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setTemplate(new File("./src/test/resources/template-directory"));

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateAdocHtml() throws IOException {
        HtmlExporter exporter = new HtmlExporter();

        File adoc = new File("./src/test/resources/generateAdoc.adoc");
        File html = new File("./target/generateAdocHtml.html");
        exporter.export(adoc, html);
    }

    @Test
    public void generateAdocPdf() throws IOException {
        HtmlExporter exporter = new HtmlExporter();

        File adoc = new File("./src/test/resources/generateAdoc.adoc");
        File html = new File("./target/generateAdocPdf.pdf");
        exporter.export(adoc, html);
    }

    @Test
    public void generateHtmlWithCustomLogListener() throws Exception {
        GenerateOptions opts = prepareOptions("generateHtmlWithCustomLogListener");
        opts.setSourceDirectory(List.of(new File("./src/test/resources/objects")));
        opts.setInclude(Arrays.asList("generateHtmlWithCustomLogListener.xml"));

        List<String> messages = new ArrayList<>();

        LogListener listener = (level, message, details) -> messages.add(level.name() + ":\t" + message + ":\t" + details);

        Generator generator = new Generator(opts);
        generator.setLogListener(listener);
        generator.generate();

        messages.forEach(m -> LOG.info(m));

        assertEquals(2, messages.size());
    }
}
