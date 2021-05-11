package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.studio.impl.cache.XmlSchemaCacheService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointXmlSchemaProvider extends XmlSchemaProvider {

    private static final Logger LOG = Logger.getInstance(MidPointXmlSchemaProvider.class);

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        if (file == null || file.getRootTag() == null) {
            return false;
        }

        String namespace = file.getRootTag().getNamespace();
        if (namespace == null) {
            return false;
        }

        if (!namespace.startsWith("http://midpoint.evolveum.com") && !namespace.startsWith("http://prism.evolveum.com")) {
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        Project project = baseFile.getProject();
        if (project == null) {
            return null;
        }

        XmlSchemaCacheService service = project.getService(XmlSchemaCacheService.class);
        return service.getSchema(url, baseFile);
    }
}
