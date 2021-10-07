package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.studio.cmd.StudioCmdMain;
import org.junit.jupiter.api.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTest {

    @Test
    public void listOptions() throws Exception {
        StudioCmdMain.main(new String[]{
                "-h"
        });
    }

    @Test
    public void simpleUpload() throws Exception {
        StudioCmdMain.main(new String[]{
                "-U",
                "http://localhost:8080/midpoint",
                "-u",
                "administrator",
                "-p",
                "5ecr3t",
                "upload",
                "-r",
                "-o",
                "-i",
                "-d",
                "@./src/test/resources/user.xml"
        });
    }
}
