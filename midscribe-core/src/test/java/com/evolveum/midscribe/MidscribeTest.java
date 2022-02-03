package com.evolveum.midscribe;

import com.evolveum.midscribe.generator.ExportFormat;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.InMemoryObjectStore;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.testng.AssertJUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class MidscribeTest {

    protected GenerateOptions prepareOptions(String name) {
        GenerateOptions opts = new GenerateOptions();
        opts.setSourceDirectory(new File("./src/test/resources/objects"));
        opts.setAdocOutput(new File("./target/" + name + ".adoc"));
        opts.setExportOutput(new File("./target/" + name + ".html"));
        opts.setExportFormat(ExportFormat.HTML);
        opts.setObjectStoreInstance(new InMemoryObjectStore(opts));

        return opts;
    }

    public static void assertFilesContentEqual(File expected, File real) throws IOException {
        assertFileExist(expected, "expected doesn't exists or it's not a file or it's not readable");
        assertFileExist(real, "real doesn't exists or it's not a file or it's not readable");

        try (
                InputStream expectedStream = new FileInputStream(expected);
                InputStream realStream = new FileInputStream(real)
        ) {
            String expectedContent = IOUtils.toString(expectedStream, StandardCharsets.UTF_8);
            String realContent = IOUtils.toString(realStream, StandardCharsets.UTF_8);

            AssertJUnit.assertEquals("Files '" + expected.getPath() + "' and '" + real.getPath() + "' doesn't match", expectedContent, realContent);
        }
    }

    private static void assertFileExist(File file, String message) {
        Validate.notNull(file, "file must not be null");

        AssertJUnit.assertTrue(message, file.exists() && file.isFile() && file.canRead());
    }

}
