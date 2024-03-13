package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.studio.impl.lang.annotation.MidPointAnnotator;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public abstract class MidPointAnnotatorIntention extends PsiElementBaseIntentionAction implements Annotator, MidPointAnnotator {

    public MidPointAnnotatorIntention(String name) {
        setText(name);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    protected XmlTag createTag(XmlTag parent, QName name) {
        return createTag(parent, name, null);
    }

    protected XmlTag createTag(XmlTag parent, QName name, String body) {
        String prefix = parent.getPrefixByNamespace(name.getNamespaceURI());

        String tagPrefix = StringUtils.isBlank(prefix) ? "" : prefix + ":";
        String namespace = prefix == null ? name.getNamespaceURI() : null;

        XmlTag child = parent.createChildTag(tagPrefix + name.getLocalPart(), namespace, body, false);

        return parent.addSubTag(child, false);
    }

    protected void createTagAnnotations(XmlTag tag, AnnotationHolder holder, String msg) {
        createTagAnnotations(tag, holder, HighlightSeverity.WARNING, msg, this);
    }
}
