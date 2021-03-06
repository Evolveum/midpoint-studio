package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Expander {

    public static final String KEY_FILE_NAME = "@filename";

    public static final String KEY_PROJECT_NAME = "#project.name";

    public static final String KEY_PROJECT_DIR = "#project.dir";

    public static final String KEY_SERVER_DISPLAY_NAME = "#server.displayName";

    public static final Pattern PATTERN = Pattern.compile("\\$\\((\\S*?)\\)");

    private Environment environment;

    private EncryptionService encryptionService;

    private EnvironmentProperties environmentProperties;

    private Map<String, String> projectProperties = new HashMap<>();

    public Expander(Environment environment, EncryptionService encryptionService, Project project) {
        this.environment = environment;
        this.encryptionService = encryptionService;
        this.environmentProperties = new EnvironmentProperties(environment);

        initProjectProperties(project);
    }

    public Environment getEnvironment() {
        return environment;
    }

    private void initProjectProperties(Project project) {
        if (project == null) {
            return;
        }

        projectProperties.put(KEY_SERVER_DISPLAY_NAME, environment.getName());
        projectProperties.put(KEY_PROJECT_NAME, project.getName());
        projectProperties.put(KEY_PROJECT_DIR, project.getBasePath());
    }

    public String expand(String object) {
        return expand(object, null);
    }

    /**
     * Options that will be translated
     * $(KEY)
     * $(username:KEY)
     * $(password:KEY)
     *
     * @param object
     * @return
     */
    public String expand(String object, VirtualFile file) {
        if (object == null) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(object);

        List<String> missingKeys = new ArrayList<>();

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key.isEmpty()) {
                matcher.appendReplacement(sb, "");
                continue;
            }

            String value = expandKey(key, file);
            if (value == null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                missingKeys.add(key);
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public InputStream expand(InputStream is, Charset charset) throws IOException {
        return expand(is, charset, null);
    }

    public InputStream expand(InputStream is, Charset charset, String fileName) throws IOException {
        if (is == null) {
            return null;
        }

        String text = IOUtils.toString(is, charset);
        String expanded = expand(text);

        return new ByteArrayInputStream(expanded.getBytes());
    }

    public boolean isEncrypted(String key) {
        EncryptedProperty property = encryptionService.get(key, EncryptedProperty.class);
        if (property == null) {
            return false;
        }

        if (property.getEnvironment() != null && environment != null) {
            if (!Objects.equals(property.getEnvironment(), environment.getId())) {
                return false;
            }
        }

        return true;
    }

    public boolean isExpandingFile(String key, VirtualFile file) {
        if (key != null && key.startsWith("@")) {
            String filePath = key.replaceFirst("@", "");
            File contentFile = new File(filePath);
            if (contentFile.isAbsolute() && contentFile.exists()) {
                return true;
            } else {
                if (file != null) {
                    if (!file.isDirectory()) {
                        file = file.getParent();
                    }
                    VirtualFile content = file.findFileByRelativePath(contentFile.getPath());
                    return content.exists();
                }
            }
        }

        return false;
    }

    public String expandKeyFromProperties(String key) {
        return expandKeyFromProperties(key, false);
    }

    public Set<String> getKeys() {
        Set<String> set = new HashSet<>();
        if (encryptionService != null) {
            set.addAll(encryptionService.list(EncryptedProperty.class).stream().map(p -> p.getKey()).collect(Collectors.toSet()));
        }
        if (projectProperties != null) {
            set.addAll(projectProperties.keySet());
        }
        if (environmentProperties != null) {
            set.addAll(environmentProperties.getKeys());
        }

        return set;
    }

    private String expandKey(String key, VirtualFile file) {
        if (key != null && key.startsWith("@")) {
            String filePath = key.replaceFirst("@", "");
            File contentFile = new File(filePath);
            if (contentFile.isAbsolute()) {
                VirtualFile content = VfsUtil.findFileByIoFile(contentFile, true);
                return loadContent(content);
            } else {
                if (file != null) {
                    if (!file.isDirectory()) {
                        file = file.getParent();
                    }
                    VirtualFile content = file.findFileByRelativePath(contentFile.getPath());

                    return loadContent(content);
                } else {
                    throw new IllegalStateException("Couldn't load file '" + key + "', unknown path '" + key + "'");
                }
            }
        }

        String value;
        value = projectProperties.get(key);
        if (value != null) {
            return value;
        }

        if (encryptionService == null || !encryptionService.isAvailable()) {
            return expandKeyFromProperties(key, true);
        }

        EncryptedProperty property = encryptionService.get(key, EncryptedProperty.class);
        if (property == null) {
            return expandKeyFromProperties(key, true);
        }

        if (property.getEnvironment() != null && environment != null) {
            if (!Objects.equals(property.getEnvironment(), environment.getId())) {
                return expandKeyFromProperties(key, true);
            }
        }

        value = property.getValue();

        if (value != null) {
            return value;
        }

        return expandKeyFromProperties(key, true);
    }

    private String expandKeyFromProperties(String key, boolean throwExceptionIfNotFound) {
        String value = environmentProperties.get(key);

        if (value == null && throwExceptionIfNotFound) {
            throw new IllegalStateException("Couldn't translate key '" + key + "'");
        }

        return value;
    }

    private String loadContent(VirtualFile file) {
        if (file.isDirectory()) {
            throw new IllegalStateException("Can't load content, file '" + file.getPath() + "' is directory");
        }

        try {
            byte[] content = file.contentsToByteArray();
            return new String(content, file.getCharset());
        } catch (IOException ex) {
            throw new IllegalStateException("Couldn't load content of file '\" + file.getPath() + \"', reason: " + ex.getMessage());
        }
    }
}
