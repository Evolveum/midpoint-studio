package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.evolveum.midpoint.studio.MidPointIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointIconProvider implements DumbAware, FileIconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (project == null) {
            return null;
        }

        PsiManager manager = PsiManager.getInstance(project);
        if (manager == null) {
            return null;
        }

        PsiFile psiFile = manager.findFile(file);
        if (!(psiFile instanceof XmlFile)) {
            return null;
        }

        XmlFile xmlFile = (XmlFile) psiFile;
        String namespace = xmlFile.getRootTag().getNamespace();
        if (!SchemaConstantsGenerated.NS_COMMON.equals(namespace)) {
            return null;
        }

        return MidPointIcons.ACTION_MIDPOINT;
    }
}
