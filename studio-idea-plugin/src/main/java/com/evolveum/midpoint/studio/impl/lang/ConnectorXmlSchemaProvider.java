package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.impl.cache.ConnectorXmlSchemaCacheService;
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
public class ConnectorXmlSchemaProvider extends XmlSchemaProvider {

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        if (file == null || file.getRootTag() == null) {
            return false;
        }

        return SchemaConstantsGenerated.NS_COMMON.equals(file.getRootTag().getNamespace());
    }

    @Nullable
    @Override
    public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        Project project = baseFile.getProject();
        if (project == null) {
            return null;
        }

        ConnectorXmlSchemaCacheService cache = project.getService(ConnectorXmlSchemaCacheService.class);
        return cache.getSchema(url, baseFile);
    }
}
