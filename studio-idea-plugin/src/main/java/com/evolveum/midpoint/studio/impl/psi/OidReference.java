package com.evolveum.midpoint.studio.impl.psi;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReference extends PsiReferenceBase<XmlAttributeValue> {

    public OidReference(XmlAttributeValue element) {
        super(element, true);
    }

    @Override
    public @Nullable PsiFile resolve() {
        Project project = getElement().getProject();

        String oid = getElement().getValue();

        VirtualFile file = ObjectFileBasedIndexImpl.getVirtualFile(oid, project);
        if (file == null) {
            return null;
        }

        return PsiManager.getInstance(project).findFile(file);
    }
}
