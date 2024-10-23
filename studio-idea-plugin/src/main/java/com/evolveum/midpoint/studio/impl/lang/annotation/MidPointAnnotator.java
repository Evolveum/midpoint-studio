package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xml.util.XmlTagUtil;

public interface MidPointAnnotator {

    default void createTagAnnotations(XmlTag tag, AnnotationHolder holder, HighlightSeverity severity, String msg, IntentionAction fix) {
        createTagAnnotations(tag, holder, severity, msg, null, fix);
    }

    default void createTagAnnotations(XmlTag tag, AnnotationHolder holder, HighlightSeverity severity, String msg, String tooltip, IntentionAction fix) {
        createNewAnnotation(XmlTagUtil.getStartTagNameElement(tag), holder, severity, msg, tooltip, fix);
        createNewAnnotation(XmlTagUtil.getEndTagNameElement(tag), holder, severity, msg, tooltip, fix);
    }

    default void createNewAnnotation(XmlToken token, AnnotationHolder holder, HighlightSeverity severity, String msg, String tooltip, IntentionAction fix) {
        if (token == null) {
            return;
        }

        if (tooltip == null) {
            tooltip = msg;
        }

        AnnotationBuilder builder = holder.newAnnotation(severity, msg)
                .range(token)
                .tooltip(tooltip);

        if (fix != null) {
            builder = builder.withFix(fix);
        }

        builder.create();
    }
}
