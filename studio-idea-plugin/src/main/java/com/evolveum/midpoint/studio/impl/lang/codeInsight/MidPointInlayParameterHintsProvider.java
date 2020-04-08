package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointInlayParameterHintsProvider implements InlayParameterHintsProvider {

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
//        XmlTag tag = getTag(value);
//        if (!isObjectTemplateOidRef(tag)) {
//            return;
//        }

        int offset = inlayOffset(element, false);

        // todo implement his inlay parameter hints correctly for oid references
//        return Arrays.asList(
//                new InlayInfo("vilo", offset, false, true, false),
//                new InlayInfo("jano", inlayOffset(element, true), false, true, true)
//        );
        return Collections.emptyList();
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
