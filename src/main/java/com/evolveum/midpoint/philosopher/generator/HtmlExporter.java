package com.evolveum.midpoint.philosopher.generator;

import org.apache.commons.io.FileUtils;
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
    public void export(File adocFile, File output) throws IOException {
        File dir = output.getParentFile();
        File file = new File(output.getName());

        FileUtils.forceMkdir(dir);

        if (output.exists()) {
            output.delete();
        }
        output.createNewFile();

        OptionsBuilder builder = OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .toDir(dir)
                .toFile(file)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes());
//                .templateDir(new File("./src/test/resources/css"));

        Map<String, Object> options = builder.asMap();

        File adoc = new File("./src/test/resources/test.adoc");

        Asciidoctor doctor = Asciidoctor.Factory.create();
        doctor.convertFile(adoc, options);
    }
}
