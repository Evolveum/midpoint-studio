package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.impl.cache.ConnectorXmlSchemaCacheService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ConnectorXmlSchemaProvider extends XmlSchemaProvider {

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        if (file == null || file.getRootTag() == null) {
            return false;
        }

        QName root = MidPointUtils.createQName(file.getRootTag());
        if (DOMUtil.XSD_SCHEMA_ELEMENT.equals(root)) {
            String fileName = file.getVirtualFile().getName();
            String uuid = fileName.replaceFirst("^connector-", "").replaceFirst("-schema.xsd$", "");

            if (MidPointUtils.UUID_PATTERN.matcher(uuid).matches()) {
                return true;
            }
        }

        return SchemaConstantsGenerated.NS_COMMON.equals(root.getNamespaceURI());
    }

    @Nullable
    @Override
    public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        Project project = baseFile.getProject();
        if (project == null || !(baseFile instanceof XmlFile)) {
            return null;
        }

        ConnectorXmlSchemaCacheService cache = project.getService(ConnectorXmlSchemaCacheService.class);
        return cache.getSchema(url, (XmlFile) baseFile);
    }
}
