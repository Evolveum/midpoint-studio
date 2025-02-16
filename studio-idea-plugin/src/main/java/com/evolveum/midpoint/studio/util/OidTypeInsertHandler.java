package com.evolveum.midpoint.studio.util;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Objects;

/**
 * This insert handler is used when user selects OID from completion list,
 * type attribute is inserted or updated as well.
 */
public class OidTypeInsertHandler implements InsertHandler<LookupElement> {

    private final QName type;

    public OidTypeInsertHandler(@NotNull QName type) {
        this.type = type;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement element) {
        PsiFile file = context.getFile();
        PsiElement psiElement = file.findElementAt(context.getStartOffset());
        XmlTag tag = psiElement != null ? PsiTreeUtil.getParentOfType(psiElement, XmlTag.class) : null;
        if (tag == null) {
            return;
        }

        QName currentType = PsiUtils.getTypeFromReferenceTag(tag);
        if (Objects.equals(currentType, type)) {
            return;
        }

        String value = type.getLocalPart();
        String prefix = tag.getPrefixByNamespace(type.getNamespaceURI());
        if (StringUtils.isNotEmpty(prefix)) {
            value = prefix + ":" + value;
        }

        tag.setAttribute("type", value);
    }
}
