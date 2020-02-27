package com.evolveum.midscribe.cmd;

import org.testng.annotations.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CmdTest {

    @Test
    public void generate() {
        String[] args = {
                "generate",
                "-s",
                "/Users/lazyman/Work/monoted/projects/ek/git/midpoint-project/objects",
                "-o",
                "./local.html",
                "-ef",
                "HTML"
        };

        MidScribeMain.main(args);
    }
}
