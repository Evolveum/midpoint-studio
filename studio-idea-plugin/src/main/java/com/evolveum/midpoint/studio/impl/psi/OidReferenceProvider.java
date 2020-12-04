package com.evolveum.midpoint.studio.impl.psi;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (MidPointUtils.isItObjectTypeOidAttribute(element)) {
            return new PsiReference[0];
        }

        XmlAttributeValue attrValue = (XmlAttributeValue) element;
        String oid = attrValue.getValue();

        List<VirtualFile> files = ObjectFileBasedIndexImpl.getVirtualFiles(oid, element.getProject());
        if (files == null) {
            return new PsiReference[0];
        }

        List<OidReference> references = files.stream().map(f -> new OidReference(attrValue, f)).collect(Collectors.toList());

        return references.toArray(new PsiReference[references.size()]);
    }
}
