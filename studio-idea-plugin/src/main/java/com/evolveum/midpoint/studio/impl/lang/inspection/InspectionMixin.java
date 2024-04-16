package com.evolveum.midpoint.studio.impl.lang.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xml.util.XmlTagUtil;

public interface InspectionMixin {

    default void registerTagProblems(
            XmlTag tag, ProblemsHolder holder, ProblemHighlightType highlightType, String msg) {
        registerTagProblems(tag, holder, highlightType, msg, null);
    }

    default void registerTagProblems(
            XmlTag tag, ProblemsHolder holder, ProblemHighlightType highlightType, String msg, String tooltip) {

        registerNewProblem(XmlTagUtil.getStartTagNameElement(tag), holder, highlightType, msg, tooltip);
        registerNewProblem(XmlTagUtil.getEndTagNameElement(tag), holder, highlightType, msg, tooltip);
    }

    default void registerNewProblem(
            XmlToken token, ProblemsHolder holder, ProblemHighlightType highlightType, String msg, String tooltip) {

        if (token == null) {
            return;
        }

        if (tooltip == null) {
            tooltip = msg;
        }

        holder.registerProblem(token, msg, highlightType);
    }
}
