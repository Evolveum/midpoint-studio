package com.evolveum.midpoint.studio.gradle;

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class StudioPluginTestBase {

    @TempDir
    protected File testProjectPath;

    protected File buildFile;

    protected File settingsFile;

    @BeforeEach
    protected void setup() throws Exception {
        buildFile = new File(testProjectPath, "build.gradle.kts");

        String content = "" +
                "plugins {\n" +
                "    id(\"com.evolveum.midpoint.studio\") version \"1.0-SNAPSHOT\"\n" +
                "}\n" +
                "\n" +
                "group = \"org.example\"\n" +
                "version = \"0.1\"\n";

        setupFile(buildFile, content);

        settingsFile = new File(testProjectPath, "settings.gradle.kts");

        content = "" +
                "rootProject.name = \"midpoint-studio-project\"\n" +
                "\n" +
                "pluginManagement {\n" +
                "    repositories {\n" +
                "        mavenLocal()\n" +
                "    }\n" +
                "}\n";

        setupFile(settingsFile, content);
    }

    protected void setupFile(File file, String content) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }

        file.createNewFile();

        FileUtils.write(file, content, StandardCharsets.UTF_8);
    }
}
