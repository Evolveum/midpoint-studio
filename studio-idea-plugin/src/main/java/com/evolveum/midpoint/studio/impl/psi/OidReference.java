package com.evolveum.midpoint.studio.impl.psi;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReference extends PsiReferenceBase<XmlElement> {

    private final VirtualFile file;

    public OidReference(XmlElement element, VirtualFile file) {
        super(element, true);

        this.file = file;
    }

    @Override
    public @Nullable PsiFile resolve() {
        if (file == null) {
            return null;
        }

        return PsiManager.getInstance(getElement().getProject()).findFile(file);
    }
}
