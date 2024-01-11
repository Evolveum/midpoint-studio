package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

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
        PsiElement parent = element.getParent();
        if (!(parent instanceof XmlAttributeValue attrValue) || !(attrValue.getParent().getParent() instanceof XmlTag reference)) {
            return;
        }

        String oid = attrValue.getValue();
        ObjectTypes type = Optional
                .ofNullable(MidPointUtils.getTypeFromReference(reference))
                .orElse(ObjectTypes.OBJECT);

        ActionUtils.runDownloadTask(project, List.of(
                new ObjectReferenceType()
                        .oid(oid)
                        .type(type.getTypeQName())), showOnly);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }


        PsiElement parent = element.getParent();
        return parent instanceof XmlAttributeValue
                && parent.getParent() instanceof XmlAttribute attribute
                && "oid".equals(attribute.getLocalName());
    }
}
