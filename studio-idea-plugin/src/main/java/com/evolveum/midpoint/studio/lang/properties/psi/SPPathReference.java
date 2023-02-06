package com.evolveum.midpoint.studio.lang.properties.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
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
        String pathString = path.getText();
        if (StringUtils.isEmpty(pathString)) {
            return null;
        }

        PsiFile spPsiFile = path.getContainingFile();
        if (spPsiFile == null) {
            return null;
        }

        VirtualFile spFile = spPsiFile.getVirtualFile();
        if (spFile == null) {
            return null;
        }

        VirtualFile file = null;

        Path ioPath = Path.of(pathString);
        if (!ioPath.isAbsolute()) {
            VirtualFile spFileDirectory = spFile.getParent();
            if (spFileDirectory == null) {
                // we'll try to check whether we're injected inside other psi (language), e.g. in xml
                PsiLanguageInjectionHost injectionHost = InjectedLanguageManager.getInstance(path.getProject()).getInjectionHost(path);
                if (injectionHost != null) {
                    PsiFile injectionHostFile = injectionHost.getContainingFile();
                    if (injectionHostFile != null && injectionHostFile.getVirtualFile() != null) {
                        spFileDirectory = injectionHostFile.getVirtualFile().getParent();
                    }
                }
            }

            if (spFileDirectory != null) {
                file = spFileDirectory.findFileByRelativePath("/" + pathString);
            }
        } else {
            file = VfsUtil.findFile(ioPath, true);
        }

        if (file == null) {
            return null;
        }

        return PsiManagerEx.getInstanceEx(path.getProject()).findFile(file);
    }
}
