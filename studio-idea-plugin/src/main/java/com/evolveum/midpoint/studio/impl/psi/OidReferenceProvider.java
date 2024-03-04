package com.evolveum.midpoint.studio.impl.psi;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!PsiUtils.isReferenceOidAttributeValue(element) && !PsiUtils.isReferenceOidTag(element)) {
            return PsiReference.EMPTY_ARRAY;
        }

        XmlTag ref = PsiUtils.findObjectReferenceTag(element);
        if (ref == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        String oid = PsiUtils.getOidFromReferenceTag(ref);
        if (oid == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        boolean isObjectOid = element instanceof XmlAttributeValue ?
                MidPointUtils.isItObjectTypeOidAttribute(element) : false;

        List<VirtualFile> files = ObjectFileBasedIndexImpl.getVirtualFiles(oid, element.getProject(), isObjectOid);

        Stream<VirtualFile> stream = files.stream();
        if (files.size() == 1 && isObjectOid) {
            stream = stream.filter(f -> !f.equals(element.getContainingFile().getVirtualFile()));
        }

        return stream
                .map(f -> new OidReference((XmlElement) element, f))
                .toList()
                .toArray(new PsiReference[0]);
    }
}
