package com.evolveum.midpoint.philosopher;

import com.evolveum.midpoint.philosopher.generator.ConnectionOptions;
import com.evolveum.midpoint.philosopher.generator.GenerateAction;
import com.evolveum.midpoint.philosopher.generator.GenerateOptions;
import com.evolveum.midpoint.philosopher.generator.HtmlExporter;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorTest {

    @Test
    public void generateSampleAdoc() throws Exception {
        ConnectionOptions con = new ConnectionOptions();
        con.setUrl("https://demo.evolveum.com/midpoint/ws/rest");
        con.setUsername("administrator");
        con.setPassword("5ecr3t");

        GenerateOptions opts = new GenerateOptions();
        opts.setConnection(con);
        opts.setOutput(new File("./target/demo.adoc"));

        GenerateAction action = new GenerateAction();
        action.init(opts);

        action.execute();
    }

    @Test
    public void generateAdocHtml() throws IOException {
        HtmlExporter exporter = new HtmlExporter();

        File adoc = new File("./src/test/resources/test.adoc");
        File html = new File("./target/test.html");
        exporter.export(adoc, html);
    }
}
