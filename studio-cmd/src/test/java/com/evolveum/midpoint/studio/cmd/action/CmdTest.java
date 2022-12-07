package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.studio.cmd.StudioCmdMain;
import org.junit.jupiter.api.Test;

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

        StudioCmdMain.main(args);
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

        StudioCmdMain.main(args);
    }
}
