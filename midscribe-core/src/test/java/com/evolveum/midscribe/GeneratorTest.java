package com.evolveum.midscribe;

import com.evolveum.midscribe.generator.ExportFormat;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.Generator;
import com.evolveum.midscribe.generator.HtmlExporter;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorTest {

    @Test
    public void generateExample() throws Exception {
        GenerateOptions opts = new GenerateOptions();
        opts.setSourceDirectory(new File("./src/test/resources/objects"));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setAdocOutput(new File("./target/example.adoc"));
        opts.setExportOutput(new File("./target/example.html"));
        opts.setExportFormat(ExportFormat.HTML);

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generatePdfExample() throws Exception {
        GenerateOptions opts = new GenerateOptions();
        opts.setSourceDirectory(new File("./src/test/resources/objects"));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setAdocOutput(new File("./target/example.adoc"));
        opts.setExportOutput(new File("./target/example.pdf"));
        opts.setExportFormat(ExportFormat.PDF);

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateWithCustomZipTemplate() throws Exception {
        GenerateOptions opts = new GenerateOptions();
        opts.setSourceDirectory(new File("./src/test/resources/objects"));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setAdocOutput(new File("./target/example.adoc"));
        opts.setExportOutput(new File("./target/example.html"));
        opts.setTemplate(new File("./src/test/resources/template.zip"));
        opts.setExportFormat(ExportFormat.HTML);

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateWithCustomDirectoryTemplate() throws Exception {
        GenerateOptions opts = new GenerateOptions();
        opts.setSourceDirectory(new File("./src/test/resources/objects"));
        opts.getExclude().addAll(Arrays.asList(new String[]{"users/*.xml", "tasks/misc/*"}));
        opts.setAdocOutput(new File("./target/example.adoc"));
        opts.setExportOutput(new File("./target/example.html"));
        opts.setTemplate(new File("./src/test/resources/template-directory"));
        opts.setExportFormat(ExportFormat.HTML);

        Generator generator = new Generator(opts);
        generator.generate();
    }

    @Test
    public void generateAdocHtml() throws IOException {
        HtmlExporter exporter = new HtmlExporter();

        File adoc = new File("./src/test/resources/test.adoc");
        File html = new File("./target/test.html");
        exporter.export(adoc, html);
    }

    @Test
    public void generateAdocPdf() throws IOException {
        HtmlExporter exporter = new HtmlExporter();

        File adoc = new File("./src/test/resources/test.adoc");
        File html = new File("./target/test.pdf");
        exporter.export(adoc, html);
    }
}
