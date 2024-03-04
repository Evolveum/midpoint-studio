package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;

public class DownloadObjectIntention extends PsiElementBaseIntentionAction {

    private static final String NAME = "Download object";

    private final boolean showOnly;

    public DownloadObjectIntention() {
        this(NAME, false);
    }

    protected DownloadObjectIntention(String text, boolean showOnly) {
        setText(text);

        this.showOnly = showOnly;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        XmlTag ref = PsiUtils.findObjectReferenceTag(element);
        if (ref == null) {
            return;
        }

        String oid = PsiUtils.getOidFromReferenceTag(ref);
        QName type = PsiUtils.getTypeFromReferenceTag(ref, ObjectType.COMPLEX_TYPE);

        ActionUtils.runDownloadTask(project, List.of(
                new ObjectReferenceType()
                        .oid(oid)
                        .type(type)), showOnly);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        XmlTag ref = PsiUtils.findObjectReferenceTag(element);
        if (ref == null) {
            return false;
        }

        String oid = PsiUtils.getOidFromReferenceTag(ref);

        return oid != null;
    }
}
