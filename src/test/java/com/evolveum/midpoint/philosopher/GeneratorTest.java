package com.evolveum.midpoint.philosopher;

import com.evolveum.midpoint.philosopher.generator.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorTest {

    @Test
    public void generateLocalAdoc() throws Exception {
        LocalOptions local = new LocalOptions();
        local.setSourceDirectory(new File("/Users/lazyman/Work/monoted/projects/ek/git/midpoint-project/objects"));

        GenerateOptions opts = new GenerateOptions();
        opts.setLocal(local);
        File adoc = new File("./target/local.adoc");
        opts.setOutput(adoc);

        GenerateAction action = new GenerateAction();
        action.init(opts);

        action.execute();

        // export to html
        HtmlExporter exporter = new HtmlExporter();

        File html = new File("./target/local.html");
        exporter.export(adoc, html);
    }

    @Test
    public void generateSampleAdoc() throws Exception {
        RemoteOptions con = new RemoteOptions();
        con.setUrl("https://demo.evolveum.com/midpoint/ws/rest");
        con.setUsername("administrator");
        con.setPassword("5ecr3t");

        GenerateOptions opts = new GenerateOptions();
        opts.setRemote(con);
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
