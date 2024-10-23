package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
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
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return null;
        }

        PsiManager manager = PsiManager.getInstance(project);
        PsiFile psiFile = manager.findFile(file);

        return MidPointUtils.isMidpointFile(psiFile) ? MidPointIcons.Midpoint : null;
    }
}
