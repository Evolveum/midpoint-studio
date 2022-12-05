package com.evolveum.midscribe.cmd;

import org.testng.annotations.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CmdTest {

    @Test
    public void generateHtml() {
        String[] args = {
                "-v",
                "generate",
                "-s",
                "../midscribe-core/src/test/resources/objects",
                "-eo",
                "./target/example.html",
                "-ef",
                "HTML",
                "-pf",
                "./src/test/resources/example.properties"
        };

        MidScribeMain.main(args);
    }

    @Test
    public void generatePdf() {
        String[] args = {
                "-v",
                "generate",
                "-s",
                "../midscribe-core/src/test/resources/objects",
                "-eo",
                "./target/example.pdf",
                "-ef",
                "PDF",
                "-pf",
                "./src/test/resources/example.properties"
        };

        MidScribeMain.main(args);
    }
}
