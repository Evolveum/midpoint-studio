package com.evolveum.midscribe.generator;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class HtmlExporter implements Exporter {

    // todo figure out templates

    @Override
    public String getDefaultExtension() {
        return "html";
    }

    @Override
    public void export(File adocFile, File output) throws IOException {
        File dir = output.getAbsoluteFile().getParentFile();
        File file = new File(output.getName());

        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toDir(dir)
                .toFile(file)
                .headerFooter(true)
                .build();
//                .templateDir(new File("./src/test/resources/css"));

        // this should improve performance of JRuby
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");

        Asciidoctor doctor = Asciidoctor.Factory.create();
        doctor.convertFile(adocFile, options);
    }
}
