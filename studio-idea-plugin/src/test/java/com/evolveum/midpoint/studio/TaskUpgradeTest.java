package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.xalan.xslt.EnvironmentCheck;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTest {

    private static final Logger LOG = Logger.getInstance(TaskUpgradeTest.class);

    @Test
    public void transformTask() throws Exception {
        StringWriter sw = new StringWriter();
        EnvironmentCheck check = new EnvironmentCheck();
        check.checkEnvironment(new PrintWriter(sw));

        LOG.info(sw.toString());

        File parent = new File(".");
        File datafile = new File(parent, "src/test/testData/task-upgrade/ls-1-input.xml");

        String input = Files.readString(datafile.toPath());

        String output = MidPointUtils.upgradeTaskToUseActivities(input);

        LOG.info(output);
    }
}
