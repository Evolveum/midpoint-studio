package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

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

        String oid = value.getValue();
        checkOidFormat(oid, value, holder);

        if (MidPointUtils.isObjectTypeElement(tag)) {
            checkObjectOidValidity(oid, value, holder);
        } else if (isObjectReference(tag)) {
//            checkObjectReferenceValidity(oid, value, holder);
        }
    }

    private void checkObjectOidValidity(String oid, XmlAttributeValue value, AnnotationHolder holder) {
        List<OidNameValue> result = ObjectFileBasedIndexImpl.getOidNamesByOid(value.getValue(), value.getProject());
        if (result != null && result.size() > 1) {
            holder.createErrorAnnotation(value, "Oid must be unique, found " + result.size() + " objects in total.");
            return;
        }
    }

    private void checkOidFormat(String oid, XmlAttributeValue value, AnnotationHolder holder) {
        if (StringUtils.isEmpty(oid)) {
            holder.createErrorAnnotation(value, "Oid not defined");
            return;
        }

        if (oid.length() > 36) {
            holder.createErrorAnnotation(value, "Oid must not be longer than 36 characters");
            return;
        }

        if (!MidPointUtils.UUID_PATTERN.matcher(oid).matches()) {
            holder.createWarningAnnotation(value, "Oid doesn't match UUID format");
            return;
        }
    }

    private boolean isReferenceValid() {
        // todo implement

        return true;
    }

    private boolean isObjectReference(XmlTag tag) {
        if (tag == null || !tag.isValid()) {
            return false;
        }

        PsiReference reference = tag.getReference();
        if (reference == null) {
            return false;
        }

        PsiElement element = reference.resolve();
        if (element == null || !(element instanceof XmlTag)) {
            return false;
        }

        XmlTag xsdElement = (XmlTag) element;
        QName qname = MidPointUtils.createQName(xsdElement);
        if (!qname.equals(new QName(W3C_XML_SCHEMA_NS_URI, "element").equals(xsdElement))) {
            return false;
        }

        XmlAttribute type = xsdElement.getAttribute("type");
        if (type == null) {
            return false;
        }

        String typeValue = type.getValue();
        if (typeValue != null && typeValue.endsWith(":ObjectReferenceType")) {
            // we probably doesn't have to resolve this reference further
            return true;
        }

        return false;
    }

    private XmlTag getTag(XmlAttributeValue value) {
        if (value == null || value.getParent() == null) {
            return null;
        }

        PsiElement tag = value.getParent().getParent();
        return tag instanceof XmlTag ? ((XmlTag) tag) : null;
    }
}
