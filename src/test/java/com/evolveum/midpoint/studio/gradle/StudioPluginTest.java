package com.evolveum.midpoint.studio.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
public class StudioPluginTest extends StudioPluginTestBase {

    @Test
    public void checkPluginUpdate() throws Exception {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectPath)
                .withArguments("clean", "build", "--info")
                .withPluginClasspath()
                .build();
        System.out.println(result);
    }
}
