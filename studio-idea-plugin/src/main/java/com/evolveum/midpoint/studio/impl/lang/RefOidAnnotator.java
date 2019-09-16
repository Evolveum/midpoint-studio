package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefOidAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiElement parent = element.getParent();
        if (!(element instanceof XmlAttributeValue)
                || !(parent instanceof XmlAttribute)
                || !"oid".equals(((XmlAttribute) parent).getLocalName())) {
            return;
        }

        XmlAttributeValue value = (XmlAttributeValue) element;
        XmlTag tag = getTag(value);
        if (!isObjectTemplateOidRef(tag)) {
            return;
        }

        String oidValue = value.getValue();
        if (StringUtils.isEmpty(oidValue)) {
            holder.createErrorAnnotation(element, "Oid not defined");
            return;
        }

        if (oidValue.length() > 36) {
            holder.createErrorAnnotation(element, "Oid must not be longer than 36 characters");
            return;
        }

        if (!MidPointUtils.UUID_PATTERN.matcher(oidValue).matches()) {
            holder.createWarningAnnotation(element, "Oid doesn't match UUID format");
            return;
        }

        // todo implement
//        if (isReferenceValid()) {
//            // todo check reference and throw error if necessary
//            holder.createInfoAnnotation(element, "Some object name");
//        } else {
//            holder.createWarningAnnotation(element, "Reference not valid. Object with this oid doesn't exist");
//        }
    }

    private boolean isReferenceValid() {
        // todo implement

        return true;
    }

    private boolean isObjectTemplateOidRef(XmlTag tag) {
        if (tag == null) {
            return false;
        }

        // todo implement
        return true;
    }

    private XmlTag getTag(XmlAttributeValue value) {
        if (value == null || value.getParent() == null) {
            return null;
        }

        PsiElement tag = value.getParent().getParent();
        return tag instanceof XmlTag ? ((XmlTag) tag) : null;
    }
}
