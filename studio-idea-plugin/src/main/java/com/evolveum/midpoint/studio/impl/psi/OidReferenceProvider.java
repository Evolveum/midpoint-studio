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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidReferenceProvider extends PsiReferenceProvider {

    // todo for object[oid] show only data from /objects folder maybe?
    @NotNull
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof XmlAttributeValue)) {
            return new PsiReference[0];
        }

        boolean isObjectOid = MidPointUtils.isItObjectTypeOidAttribute(element);

        XmlAttributeValue attrValue = (XmlAttributeValue) element;
        String oid = attrValue.getValue();

        List<VirtualFile> files = ObjectFileBasedIndexImpl.getVirtualFiles(oid, element.getProject(), isObjectOid);
        if (files == null) {
            return new PsiReference[0];
        }

        Stream<VirtualFile> stream = files.stream();
        if (files.size() == 1 && isObjectOid) {
            stream = stream.filter(f -> !f.equals(element.getContainingFile().getVirtualFile()));
        }

        List<OidReference> references = stream.map(f -> new OidReference(attrValue, f)).collect(Collectors.toList());

        return references.toArray(new PsiReference[references.size()]);
    }
}
