package com.evolveum.midpoint.studio.impl.lang.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlTagUtil;

public interface InspectionMixin {

    default void registerTagProblems(
            XmlTag tag, ProblemsHolder holder, ProblemHighlightType highlightType, String msg) {

        registerTokenProblem(XmlTagUtil.getStartTagNameElement(tag), holder, highlightType, msg);
        registerTokenProblem(XmlTagUtil.getEndTagNameElement(tag), holder, highlightType, msg);
    }

    default void registerTokenProblem(
            XmlElement token, ProblemsHolder holder, ProblemHighlightType highlightType, String msg) {

        if (token == null) {
            return;
        }

        holder.registerProblem(token, msg, highlightType);
    }
}
