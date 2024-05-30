package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.Expander;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentPropertiesCache extends Cache {

    private static final Logger LOG = Logger.getInstance(EnvironmentPropertiesCache.class);

    private Expander expander;

    public EnvironmentPropertiesCache(Project project) {
        super(project);

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {

            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                if (expander == null || expander.getEnvironment() == null) {
                    return;
                }

                Environment env = expander.getEnvironment();
                if (env == null || StringUtils.isEmpty(env.getPropertiesFilePath())) {
                    return;
                }

                File file = new File(env.getPropertiesFilePath());
                if (!file.exists() || file.isDirectory() || !file.canRead()) {
                    return;
                }

                VirtualFile envFile = VfsUtil.findFileByIoFile(file, false);

                boolean refresh = false;
                for (VFileEvent e : events) {
                    if (e.getFile() != null && e.getFile().equals(envFile)) {
                        refresh = true;
                        break;
                    }
                }

                if (refresh) {
                    reload();
                }
            }
        });
    }

    @Override
    void clear() {
        LOG.debug("Clearing cache for {}", getClass().getSimpleName());

        expander = null;

        LOG.debug("Cache cleared for {}", getClass().getSimpleName());
    }

    @Override
    void reload() {
        LOG.debug("Refreshing cache for {}", getClass().getSimpleName());

        if (getEnvironment() == null) {
            expander = null;
            return;
        }

        expander = new Expander(getEnvironment(), getProject());

        // force re-highlight editors, this probably shouldn't be here, but right now no better place
        ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
        DaemonCodeAnalyzer dca = DaemonCodeAnalyzer.getInstance(getProject());
        dca.restart();

        LOG.debug("Refresh finished for {}", getClass().getSimpleName());
    }

    public String expandKeyForInlay(String key, VirtualFile file) {
        if (expander == null) {
            return null;
        }

        if (expander.isEncrypted(key)) {
            return "Encrypted";
        }

        if (expander.isExpandingFile(key, file)) {
            return "File";
        }

        return expander.expandKeyFromProperties(key);
    }

    public Set<String> getKeys() {
        if (expander == null) {
            return new HashSet<>();
        }

        return expander.getKeys();
    }
}
