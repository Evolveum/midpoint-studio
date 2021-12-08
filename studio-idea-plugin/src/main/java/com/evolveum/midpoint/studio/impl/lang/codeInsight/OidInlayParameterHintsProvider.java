package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidInlayParameterHintsProvider implements InlayParameterHintsProvider {

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(PsiElement element) {
        PsiElement parent = element.getParent();
        if (!(element instanceof XmlAttributeValue)
                || !(parent instanceof XmlAttribute)
                || !"oid".equals(((XmlAttribute) parent).getLocalName())) {
            return Collections.emptyList();
        }

        XmlAttributeValue value = (XmlAttributeValue) element;
        int offset = inlayOffset(element, false);

        XmlAttribute attr = (XmlAttribute) parent;
        XmlTag tag = attr.getParent();
        if (MidPointUtils.isObjectTypeElement(tag, false)) {    // check even without namespaces MID-7468
            return Collections.emptyList();
        }

        List<OidNameValue> result = ObjectFileBasedIndexImpl.getOidNamesByOid(value.getValue(), element.getProject());
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }

        String label;
        if (result.size() == 1) {
            label = result.iterator().next().getName();
            if (StringUtils.isEmpty(label)) {
                label = "Name undefined";
            }
        } else {
            boolean multiple = false;

            label = result.iterator().next().getName();
            for (OidNameValue o : result) {
                if (!Objects.equals(label, o.getName())) {
                    multiple = true;
                    break;
                }
            }

            if (multiple) {
                label = "Multiple object with same oid";
            } else {
                label = label + " (multiple objects)";
            }

        }

        return Arrays.asList(new InlayInfo(label, offset, false, true, false));
    }

    private int inlayOffset(PsiElement expr, boolean atEnd) {
        if (expr.getTextRange().isEmpty()) {
            PsiElement next = expr.getNextSibling();
            if (next instanceof PsiWhiteSpace) {
                return next.getTextRange().getEndOffset();
            }
        }

        if (atEnd) {
            return expr.getTextRange().getEndOffset();
        }

        return expr.getTextRange().getStartOffset();
    }

    @Nullable
    @Override
    public HintInfo getHintInfo(PsiElement element) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
