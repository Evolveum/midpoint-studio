package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointXmlSchemaProvider extends XmlSchemaProvider {

    private static final Logger LOG = Logger.getInstance(MidPointXmlSchemaProvider.class);

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        return true; //MidPointFileTypeFactory.isMidPoint(file);
    }

    @Nullable
    @Override
    public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        if (!url.startsWith("http://midpoint.evolveum.com")
                && !url.startsWith("http://prism.evolveum.com")) {
            return null;
        }

        Project project = baseFile.getProject();

        String resourceUrl = url.replaceFirst("http://midpoint.evolveum.com", "");
        resourceUrl = resourceUrl.replaceFirst("http://prism.evolveum.com", "");

        resourceUrl += ".xsd";

        URL resource = MidPointXmlSchemaProvider.class.getResource(resourceUrl);
        if (resource == null) {
            LOG.warn("Couldn't find schema for url '" + url + "', tried '" + resourceUrl + "'");
            return null;
        }

        VirtualFile fileByURL = VfsUtil.findFileByURL(resource);
        if (fileByURL == null) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(fileByURL);
        return (XmlFile) psiFile;
    }
}
