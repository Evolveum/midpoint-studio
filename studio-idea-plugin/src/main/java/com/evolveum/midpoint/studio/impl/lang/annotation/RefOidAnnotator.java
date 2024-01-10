package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefOidAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        PsiElement parent = element.getParent();
        if (!(element instanceof XmlAttributeValue value)
                || !(parent instanceof XmlAttribute)
                || !"oid".equals(((XmlAttribute) parent).getLocalName())) {
            return;
        }

        XmlTag tag = getTag(value);

        String oid = value.getValue();
        checkOidFormat(oid, value, holder);

        if (MidPointUtils.isObjectTypeElement(tag)) {
            checkObjectOidValidity(oid, value, holder);
        } else if (isObjectReference(tag)) {
            checkObjectReferencePresence(oid, value, holder);
        }
    }

    private String getTypeFromReference(XmlTag tag) {
        String xmlType = tag.getAttributeValue("type");
        String type = "object";
        if (xmlType != null) {
            String localPart = QNameUtil.parsePrefixedName(xmlType).localName();
            ObjectTypes ot = Arrays.stream(ObjectTypes.values())
                    .filter(t -> t.getTypeQName().getLocalPart().equals(localPart))
                    .findFirst()
                    .orElse(null);
            if (ot != null) {
                type = StudioLocalization.get().translateEnum(ot);
                if (type != null) {
                    type = type.toLowerCase();
                }
            }
        }

        return type;
    }

    private void checkObjectReferencePresence(String oid, XmlAttributeValue value, AnnotationHolder holder) {
        XmlTag tag = getTag(value);
        String type = getTypeFromReference(tag);

        List<VirtualFile> result = ObjectFileBasedIndexImpl.getVirtualFiles(value.getValue(), value.getProject(), true);
        if (result.isEmpty()) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Referenced " + type + " with oid '" + oid + "' not found in project.")
                    .range(value)
                    .highlightType(ProblemHighlightType.POSSIBLE_PROBLEM)
                    .create();
        }
    }

    private void checkObjectOidValidity(String oid, XmlAttributeValue value, AnnotationHolder holder) {
        List<OidNameValue> result = ObjectFileBasedIndexImpl.getOidNamesByOid(value.getValue(), value.getProject(), true);
        if (result.size() > 1) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Oid must be unique, found " + result.size() + " objects in total.")
                    .range(value)
                    .create();
        }
    }

    private void checkOidFormat(String oid, XmlAttributeValue value, AnnotationHolder holder) {
        if (StringUtils.isEmpty(oid)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Oid not defined")
                    .range(value)
                    .create();
            return;
        }

        if (oid.length() > 36) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Oid must not be longer than 36 characters")
                    .range(value)
                    .create();
            return;
        }

        if (!MidPointUtils.UUID_PATTERN.matcher(oid).matches()) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Oid doesn't match UUID format")
                    .range(value)
                    .create();
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
        if (!(element instanceof XmlTag xsdElement)) {
            return false;
        }

        QName qname = MidPointUtils.createQName(xsdElement);
        if (!new QName(W3C_XML_SCHEMA_NS_URI, "element").equals(qname)) {
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
