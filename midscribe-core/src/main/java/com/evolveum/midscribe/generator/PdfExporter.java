package com.evolveum.midscribe.generator;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PdfExporter implements Exporter {

    private static final Logger LOG = LoggerFactory.getLogger(PdfExporter.class);

    @Override
    public String getDefaultExtension() {
        return "pdf";
    }

    @Override
    public void export(File adocFile, File output) throws IOException {
        File dir = output.getAbsoluteFile().getParentFile();
        File file = new File(output.getName());

        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .inPlace(true)
                .toDir(dir)
                .toFile(file)
                .backend("pdf")
                .build();

        // this should improve performance of JRuby
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");

        Asciidoctor doctor = Asciidoctor.Factory.create();
        doctor.convertFile(adocFile, options);
    }
}
