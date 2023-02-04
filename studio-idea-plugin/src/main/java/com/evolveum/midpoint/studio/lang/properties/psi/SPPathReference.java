package com.evolveum.midpoint.studio.lang.properties.psi;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.PsiManagerEx;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPathReference extends PsiReferenceBase<SPPath> {

    public SPPathReference(@NotNull SPPath element) {
        super(element, true);
    }

    @Override
    public @Nullable PsiElement resolve() {
        SPPath path = getElement();
        String filePath = path.getText();
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }

        PsiFile root = path.getContainingFile();
        if (root == null) {
            return null;
        }

        VirtualFile rootFile = root.getVirtualFile();
        if (rootFile == null) {
            return null;
        }

        Path ioPath = Path.of(filePath);
        VirtualFile file;
        if (!ioPath.isAbsolute()) {
            if (rootFile.getParent() == null) {
                // todo if this psi is inside of another psi (like embedded in xml file) then this is null
                return null;
            }
            file = rootFile.getParent().findFileByRelativePath("/" + filePath);
        } else {
            file = VfsUtil.findFile(ioPath, true);
        }

        if (file == null) {
            return null;
        }

        return PsiManagerEx.getInstanceEx(path.getProject()).findFile(file);
    }
}
