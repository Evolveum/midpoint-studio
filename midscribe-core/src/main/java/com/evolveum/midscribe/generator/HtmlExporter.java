package com.evolveum.midscribe.generator;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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

        OptionsBuilder builder = OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .toDir(dir)
                .toFile(file)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes());
//                .templateDir(new File("./src/test/resources/css"));

        Map<String, Object> options = builder.asMap();

        // this should improve performance of JRuby
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");

        Asciidoctor doctor = Asciidoctor.Factory.create();
        doctor.convertFile(adocFile, options);
    }
}
