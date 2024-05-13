package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

    public static final Pattern PATTERN = Pattern.compile("\\$\\((.+?)\\)");

    private Environment environment;

    private EncryptionService encryptionService;

    private EnvironmentProperties environmentProperties;

    private Map<String, String> projectProperties = new HashMap<>();

    private boolean ignoreMissingKeys;

    public Expander(Environment environment, Project project) {
        this(environment,
                project != null ? EncryptionService.getInstance(project) : null, project);
    }

    public Expander(Environment environment, EncryptionService encryptionService, Project project) {
        this.environment = environment;
        this.encryptionService = encryptionService;
        this.environmentProperties = new EnvironmentProperties(project, environment);

        MidPointService ms = MidPointService.get(project);
        this.ignoreMissingKeys = ms.getSettings().isIgnoreMissingKeys();

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
        return expand(object, (ExpanderOptions) null);
    }

    public String expand(String object, ExpanderOptions opts) {
        return expand(object, null, opts);
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
        return expand(object, file, null);
    }

    public String expand(String object, VirtualFile file, ExpanderOptions opts) {
        if (object == null) {
            return null;
        }

        if (opts == null) {
            opts = new ExpanderOptions();
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

            String value = expandKey(key, file, opts.expandEncrypted());
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
        String filePath = getFilePathFromKey(key);
        if (filePath == null) {
            return false;
        }

        File contentFile = new File(filePath);
        if (contentFile.isAbsolute() && contentFile.exists()) {
            return true;
        } else {
            if (file != null) {
                if (!file.isDirectory()) {
                    file = file.getParent();
                }
                VirtualFile content = file.findFileByRelativePath(contentFile.getPath());
                return content != null && content.exists();
            }
        }

        return false;
    }

    private String getFilePathFromKey(String key) {
        if (key == null) {
            return null;
        }

        key = key.trim();
        if (!key.startsWith("@")) {
            return null;
        }

        return key.replaceFirst("@", "").trim();
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

    private String expandKey(String key, VirtualFile file, boolean expandEncrypted) {
        String filePath = getFilePathFromKey(key);
        if (filePath != null) {
            // just windows stuff (mid-7781). Backslash is not correctly handled later in {@link VirtualFile.findFileByRelativePath(path) }
            filePath = filePath.replace("\\", "/");

            Path uri = Path.of(filePath);
            if (uri.isAbsolute()) {
                VirtualFile content = VfsUtil.findFile(uri, true);

                return loadContent(content, key, filePath, null);
            } else {
                if (file != null) {
                    if (!file.isDirectory()) {
                        file = file.getParent();
                    }
                    VirtualFile content = file.findFileByRelativePath(filePath);

                    return loadContent(content, key, filePath, file);
                } else {
                    if (ignoreMissingKeys) {
                        return null;
                    }
                    throw new IllegalStateException("Couldn't load file '" + key + "', unknown path '" + key + "'");
                }
            }
        }

        String value;
        value = projectProperties.get(key);
        if (value != null) {
            return value;
        }

        if (!expandEncrypted || encryptionService == null || !encryptionService.isAvailable()) {
            return expandKeyFromProperties(key, expandEncrypted);
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
            if (ignoreMissingKeys) {
                return null;
            }
            throw new IllegalStateException("Couldn't translate key '" + key + "'");
        }

        return value;
    }

    private String loadContent(VirtualFile file, String key, String contentFilePath, VirtualFile contentParent) {
        if (file == null) {
            if (ignoreMissingKeys) {
                return null;
            }
            throw new IllegalStateException("Can't load content for key '" + key + "', file '" + contentFilePath + "' is not present in '" + contentParent + "'");
        }

        if (file.isDirectory()) {
            if (ignoreMissingKeys) {
                return null;
            }
            throw new IllegalStateException("Can't load content for key '" + key + "', file '" + file.getPath() + "' is directory");
        }

        try {
            byte[] content = file.contentsToByteArray();
            return new String(content, file.getCharset());
        } catch (IOException ex) {
            if (ignoreMissingKeys) {
                return null;
            }
            throw new IllegalStateException("Couldn't load content for key '" + key + "', file '" + file.getPath() + "', reason: " + ex.getMessage());
        }
    }
}
