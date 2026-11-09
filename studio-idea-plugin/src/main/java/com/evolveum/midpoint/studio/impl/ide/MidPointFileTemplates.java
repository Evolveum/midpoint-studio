package com.evolveum.midpoint.studio.impl.ide;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Resolves text of file templates bundled with the plugin (fileTemplates/*.ft).
 * Template registered in IDE is preferred, so user customizations apply. When IDE
 * doesn't know the template, bundled resource is rendered directly.
 */
public class MidPointFileTemplates {

    private static final Logger LOG = Logger.getInstance(MidPointFileTemplates.class);

    private static final String RESOURCE_PREFIX = "/fileTemplates/";

    private static final String RESOURCE_SUFFIX = ".ft";

    private MidPointFileTemplates() {
    }

    public static @NotNull String text(@NotNull Project project, @NotNull String name, @Nullable Properties properties)
            throws IOException {

        FileTemplate template = FileTemplateManager.getInstance(project).getTemplate(name);
        if (template != null) {
            return properties != null ? template.getText(properties) : template.getText();
        }

        LOG.warn("File template '" + name + "' is not registered in IDE, using bundled resource");

        return bundledText(name, properties);
    }

    public static @NotNull String bundledText(@NotNull String name, @Nullable Properties properties) throws IOException {
        String resource = RESOURCE_PREFIX + name + RESOURCE_SUFFIX;

        try (InputStream is = MidPointFileTemplates.class.getResourceAsStream(resource)) {
            if (is == null) {
                throw new IOException("Bundled file template '" + resource + "' not found");
            }

            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return properties != null ? FileTemplateUtil.mergeTemplate(properties, text, false) : text;
        }
    }
}
