package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExtensionSchemaCache extends Cache {

    private static final Logger LOG = Logger.getInstance(ExtensionSchemaCache.class);

    private Map<String, XmlFile> cache = new HashMap<>();

    public ExtensionSchemaCache(@NotNull Project project) {
        super(project);
    }

    @Override
    synchronized void clear() {
        cache.clear();
    }

    @Override
    synchronized void reload() {
        LOG.info("Reloading cache");

        cache.clear();

        Environment env = getEnvironment();
        if (env == null) {
            LOG.info("Reload skipped, no environment selected");
            return;
        }

        MidPointClient client = new MidPointClient(getProject(), env, true, true);
        Map<SchemaFileType, String> schemas = client.getExtensionSchemas();

        if (schemas != null) {
            for (Map.Entry<SchemaFileType, String> entry : schemas.entrySet()) {
                SchemaFileType schemaFile = entry.getKey();

                VirtualFile file = new LightVirtualFile(schemaFile.getFileName(), entry.getValue());
                PsiFile psiFile = ApplicationManager.getApplication()
                        .runReadAction(
                                (Computable<? extends PsiFile>) () -> PsiManager.getInstance(getProject()).findFile(file));

                if (!(psiFile instanceof XmlFile)) {
                    continue;
                }

                cache.put(schemaFile.getNamespace(), (XmlFile) psiFile);
            }
        }

        LOG.info("Reload finished, " + cache.size() + " objects in cache");
    }

    public Map<String, XmlFile> getFiles() {
        return Collections.unmodifiableMap(cache);
    }

    public synchronized XmlFile getSchema(String url, PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        return cache.get(url);
    }
}
