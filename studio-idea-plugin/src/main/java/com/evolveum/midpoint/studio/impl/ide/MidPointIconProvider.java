package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointIconProvider implements DumbAware, FileIconProvider {

    public static final Set<String> NAMESPACES;

    static {
        Set<String> set = new HashSet<>();

        set.addAll(Arrays.asList(
                SchemaConstantsGenerated.NS_COMMON,
                SchemaConstantsGenerated.NS_QUERY,
                SchemaConstantsGenerated.NS_SCRIPTING
        ));

        NAMESPACES = Collections.unmodifiableSet(set);
    }

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
        if (xmlFile.getRootTag() == null) {
            return null;
        }

        String namespace = xmlFile.getRootTag().getNamespace();
        if (namespace == null || !NAMESPACES.contains(namespace)) {
            return null;
        }

        return MidPointIcons.Midpoint;
    }
}
