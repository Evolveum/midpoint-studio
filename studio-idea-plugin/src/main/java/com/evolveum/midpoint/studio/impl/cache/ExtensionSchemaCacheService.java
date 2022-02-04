package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.concurrency.AppExecutorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExtensionSchemaCacheService {

    private static final Logger LOG = Logger.getInstance(ExtensionSchemaCacheService.class);

    private Project project;

    private Map<String, XmlFile> cache = new HashMap<>();

    public ExtensionSchemaCacheService(Project project) {
        this.project = project;

        MidPointUtils.subscribeToEnvironmentChange(project, this::refresh);
    }

    public void refresh(Environment env) {
        LOG.info("Invoking refresh");

        RunnableUtils.submitNonBlockingReadAction(() -> {

            LOG.info("Refreshing");

            cache.clear();

            if (env == null) {
                LOG.info("Refresh skipped, no environment selected");
                return;
            }

            MidPointClient client = new MidPointClient(project, env, true, true);
            Map<SchemaFileType, String> schemas = client.getExtensionSchemas();

            cache.clear();
            if (schemas != null) {
                for (Map.Entry<SchemaFileType, String> entry : schemas.entrySet()) {
                    SchemaFileType schemaFile = entry.getKey();

                    VirtualFile file = new LightVirtualFile(schemaFile.getFileName(), entry.getValue());
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (!(psiFile instanceof XmlFile)) {
                        continue;
                    }

                    cache.put(schemaFile.getNamespace(), (XmlFile) psiFile);
                }
            }

            LOG.info("Refresh finished, " + cache.size() + " objects in cache");
        }, AppExecutorUtil.getAppExecutorService());

        LOG.info("Invoke done");
    }

    public synchronized XmlFile getSchema(String url, PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        return cache.get(url);
    }
}
