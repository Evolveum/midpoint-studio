package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Objects;

public class PsiUtils {

    public static boolean isXmlElement(PsiElement element) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        if (element == null || !element.isValid()) {
            return false;
        }

        PsiFile file = element.getContainingFile();
        if (!(file instanceof XmlFile)) {
            return false;
        }

        return (element instanceof XmlElement);
    }

    public static XmlTag getParentTag(PsiElement element) {
        if (element == null) {
            return null;
        }

        if (!(element instanceof XmlElement xmlElement)) {
            return null;
        }

        if (element instanceof XmlTag tag) {
            return tag;
        }

        return getParentTag(element.getParent());
    }

    /**
     * @param element
     * @return XmlTag if element is inside c:ObjectReferenceType, null otherwise
     */
    public static XmlTag findObjectReferenceTag(PsiElement element) {
        XmlTag parentTag = PsiUtils.getParentTag(element);
        if (parentTag == null) {
            return null;
        }

        // parentTag should be type of c:ObjectReferenceType or
        // <oid> with parent c:ObjectReferenceType (undocumented, experimental)
        XmlTag possibleReference = parentTag;

        QName parentTagName = MidPointUtils.createQName(parentTag);
        if (possibleReference.getParentTag() != null && (
                Objects.equals(parentTagName, ObjectReferenceType.F_OID)
                        || Objects.equals(parentTagName, ObjectReferenceType.F_TYPE))) {
            possibleReference = possibleReference.getParentTag();
        }

        XmlTag xsd = getXsdReference(possibleReference);
        if (xsd == null || xsd == possibleReference) {
            // xsd definition not found or tag points to itself (in case schema is not available or not indexed yet)
            return null;
        }

        String type = xsd.getAttributeValue("type", xsd.getNamespace());
        if (type == null) {
            return null;
        }

        QNameUtil.PrefixedName name = QNameUtil.parsePrefixedName(type);
        if (!Objects.equals(name.localName(), ObjectReferenceType.COMPLEX_TYPE.getLocalPart())) {
            return null;
        }

        String namespace = xsd.getNamespaceByPrefix(name.prefix());
        if (!Objects.equals(namespace, ObjectReferenceType.COMPLEX_TYPE.getNamespaceURI())) {
            return null;
        }

        return possibleReference;
    }

    /**
     * @param tag
     * @return Returns XSD type for given tag. Only if tag has reference to XSD element.
     * Reads type {@link QName} from attribute "type".
     */
    public static QName getTagXsdType(XmlElement tag) {
        XmlTag reference = getXsdReference(tag);
        if (reference == null) {
            return null;
        }

        String value = reference.getAttributeValue("type", reference.getNamespace());
        if (value == null) {
            return null;
        }

        return convertTextToQName(value, reference);
    }

    public static XmlTag getXsdReference(XmlElement tag) {
        PsiReference ref = tag.getReference();
        if (!(ref instanceof TagNameReference tagNameReference)) {
            return null;
        }

        PsiElement reference = tagNameReference.resolve();
        if (!(reference instanceof XmlTag xsd)) {
            return null;
        }

        return xsd;
    }

    public static QName getTypeFromReferenceTag(XmlTag reference) {
        return getTypeFromReferenceTag(reference, null);
    }

    public static QName getTypeFromReferenceTag(XmlTag reference, QName defaultType) {
        if (reference == null) {
            return defaultType;
        }

        String type = reference.getAttributeValue("type");
        if (type != null) {
            return convertTextToQName(type, reference);
        }

        return Arrays.stream(reference.getSubTags())
                .filter(tag -> Objects.equals(tag.getLocalName(), "type"))
                .map(t -> t.getValue().getText())
                .map(v -> convertTextToQName(v, reference))
                .filter(q -> q != null)
                .findFirst()
                .orElse(null);
    }

    private static QName convertTextToQName(String value, XmlTag tag) {
        QNameUtil.PrefixedName prefixed = QNameUtil.parsePrefixedName(value);
        String ns = tag.getNamespaceByPrefix(prefixed.prefix());
        return new QName(ns, prefixed.localName());
    }

    /**
     * Check whether tag is <c:oid>, doesn't check parent whether it's reference.
     *
     * @param element
     * @return
     */
    public static boolean isReferenceOidTag(PsiElement element) {
        if (element == null) {
            return false;
        }

        if (!(element instanceof XmlTag tag)) {
            return false;
        }

        QName name = MidPointUtils.createQName(tag);
        return Objects.equals(name, ObjectReferenceType.F_OID);
    }

    public static String getOidFromReferenceTag(XmlTag reference) {
        if (reference == null) {
            return null;
        }

        String oid = reference.getAttributeValue("oid");
        if (oid != null) {
            return oid;
        }

        return Arrays.stream(reference.getSubTags())
                .filter(tag -> isReferenceOidTag(tag))
                .map(t -> t.getValue().getText())
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    public static boolean isReferenceOidAttributeValue(PsiElement element) {
        if (!(element instanceof XmlAttributeValue value)) {
            return false;
        }

        if (!(value.getParent() instanceof XmlAttribute attribute)) {
            return false;
        }

        return "oid".equals(attribute.getLocalName());
    }
}
