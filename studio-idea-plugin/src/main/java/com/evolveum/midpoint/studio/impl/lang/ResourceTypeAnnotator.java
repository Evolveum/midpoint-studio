package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.SyncAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceAttributeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SynchronizationActionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SynchronizationReactionType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ResourceTypeAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof XmlFile)) {
            return;
        }

        attributeTolerantFalse(element, holder);
        situationWithoutAction(element, holder);
        synchronizationActionHandler(element, holder);
    }

    private boolean isElementInResourceObject(PsiElement element) {
        PsiFile file = element.getContainingFile();

        XmlFile xmlFile = (XmlFile) file;
        XmlTag root = xmlFile.getRootTag();
        QName name = MidPointUtils.createQName(root);
        if (SchemaConstants.C_OBJECTS.equals(name)) {
            List<XmlTag> tags = new ArrayList<>();
            PsiElement e = element;
            do {
                if (e instanceof XmlTag) {
                    tags.add((XmlTag) e);
                }
                e = e.getParent();
            } while (e.getParent() != null);

            if (tags.size() < 3) {
                return false;
            }

            XmlTag tag = tags.get(tags.size() - 2);
            QName tagName = MidPointUtils.createQName(tag);
            if (!ObjectTypes.RESOURCE.getElementName().equals(tagName)) {
                return false;
            }
        } else if (!ObjectTypes.RESOURCE.getElementName().equals(name)) {
            return false;
        }

        return true;
    }

    private void synchronizationActionHandler(PsiElement element, AnnotationHolder holder) {
        if (!(element instanceof XmlTag)) {
            return;
        }

        XmlTag e = (XmlTag) element;
        if (!SynchronizationActionType.F_HANDLER_URI.getLocalPart().equals(e.getLocalName())) {
            return;
        }

//        if (!isElementInResourceObject(element)) {
//            return;
//        }

        String uri = e.getValue().getText();
        boolean found = false;
        for (SyncAction action : SyncAction.values()) {
            if (!action.getUri().equals(uri)) {
                continue;
            }

            found = true;

            if (action.isDeprecated()) {
                holder.createWarningAnnotation(element, "Synchronization action handlerUri is deprecated");
                break;
            }
        }

        if (!found) {
            holder.createErrorAnnotation(element, "Unknown synchronization action handlerUri");
        }
    }

    private void situationWithoutAction(PsiElement element, AnnotationHolder holder) {
        if (!(element instanceof XmlTag)) {
            return;
        }

        XmlTag e = (XmlTag) element;
        if (!SynchronizationReactionType.F_SITUATION.getLocalPart().equals(e.getLocalName())) {
            return;
        }

        if (!isElementInResourceObject(element)) {
            return;
        }

        XmlTag reaction = e.getParentTag();
        // todo implement if there's only situation and no action or synchronize=true
    }

    private void attributeTolerantFalse(PsiElement element, AnnotationHolder holder) {
        if (!(element instanceof XmlTag)) {
            return;
        }

        XmlTag e = (XmlTag) element;
        if (!ResourceAttributeDefinitionType.F_TOLERANT.getLocalPart().equals(e.getLocalName())) {
            return;
        }

        if (!isElementInResourceObject(element)) {
            return;
        }

        if (!"false".equals(e.getValue().getText())) {
            return;
        }

        XmlTag attribute = e.getParentTag();
        XmlTag[] tags = attribute.getSubTags();
        boolean inboundFound = false;
        boolean outboundFound = false;
        for (XmlTag tag : tags) {
            if (!inboundFound && ResourceAttributeDefinitionType.F_INBOUND.getLocalPart().equals(tag.getLocalName())) {
                inboundFound = true;
                continue;
            }

            if (!outboundFound && ResourceAttributeDefinitionType.F_OUTBOUND.getLocalPart().equals(tag.getLocalName())) {
                outboundFound = true;
            }
        }

        if (inboundFound && !outboundFound) {
            holder.createWarningAnnotation(element, "Tolerant=false means MidPoint will try to remove values " +
                    "even if there's no outbound in this attribute mapping.");
        }
    }
}
