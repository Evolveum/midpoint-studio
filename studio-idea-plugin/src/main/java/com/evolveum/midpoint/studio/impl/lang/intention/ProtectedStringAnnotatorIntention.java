package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Objects;

public class ProtectedStringAnnotatorIntention extends MidPointAnnotatorIntention {

    private static final String NAME = "Use secrets provider";

    private static final QName PROVIDER = new QName(SchemaConstantsGenerated.NS_TYPES, "provider");
    private static final QName KEY = new QName(SchemaConstantsGenerated.NS_TYPES, "key");

    public ProtectedStringAnnotatorIntention() {
        super(NAME);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        XmlTag value = getProtectedStringTag(element.getParent());
        if (value == null) {
            return;
        }

        ThrowableRunnable<RuntimeException> task = () -> {
            XmlTag externalData = createTag(value, ProtectedStringType.F_EXTERNAL_DATA);
            createTag(externalData, PROVIDER, "INSERT_PROVIDER_NAME");
            createTag(externalData, KEY, "INSERT_KEY_NAME");

            deleteSubTag(value, ProtectedStringType.F_CLEAR_VALUE);
            deleteSubTag(value, ProtectedStringType.F_HASHED_DATA);
            deleteSubTag(value, ProtectedStringType.F_ENCRYPTED_DATA);
        };

        WriteCommandAction.writeCommandAction(project)
                .withName(NAME)
                .withGroupId(NAME)
                .run(task);
    }

    private void deleteSubTag(XmlTag parent, QName subTagName) {
        XmlTag subTag = MidPointUtils.findSubTag(parent, subTagName);
        if (subTag != null) {
            subTag.delete();
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        // are we in element that is one of the problematic
        // elements (clearValue, encryptedData, hashedData) in protected string?

        if (!(element instanceof XmlToken)) {
            return false;
        }

        if (!(element.getParent() instanceof XmlTag tag)) {
            return false;
        }

        return getClearValueTag(tag) != null
                || getEncryptedDataTag(tag) != null
                || getHashedDataTag(tag) != null;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        XmlTag clearValue = getClearValueTag(element);
        if (clearValue != null) {
            String msg = "Clear value element represents possible security problem, the reason is that it contains " +
                    "clear value which is not safe. Secret providers configuration should be used.";

            createTagAnnotations(clearValue, holder, msg);
        }

        XmlTag encryptedData = getEncryptedDataTag(element);
        if (encryptedData != null) {
            String msg = "Encrypted data element represents possible problem when moving this midPoint " +
                    "object to another environment. Secret providers configuration should be used.";

            createTagAnnotations(encryptedData, holder, msg);
        }

        XmlTag hashedData = getHashedDataTag(element);
        if (hashedData != null) {
            String msg = "Hashed data element represents possible problem when moving this midPoint " +
                    "object to another environment. Secret providers configuration should be used.";

            createTagAnnotations(hashedData, holder, msg);
        }
    }

    private XmlTag getClearValueTag(PsiElement value) {
        return getProtectedStringItem(value, ProtectedStringType.F_CLEAR_VALUE);
    }

    private XmlTag getEncryptedDataTag(PsiElement value) {
        return getProtectedStringItem(value, ProtectedStringType.F_ENCRYPTED_DATA);
    }

    private XmlTag getHashedDataTag(PsiElement value) {
        return getProtectedStringItem(value, ProtectedStringType.F_HASHED_DATA);
    }

    /**
     * @return XmlTag if {@link PsiElement} element is tag and a child inside c:ProtectedStringType with
     * specific name, null otherwise
     */
    private XmlTag getProtectedStringItem(PsiElement element, QName name) {
        if (!(element instanceof XmlTag tag)) {
            return null;
        }

        if (!Objects.equals(name, MidPointUtils.createQName(tag))) {
            return null;
        }

        XmlTag ps = getProtectedStringTag(tag);
        if (ps == null) {
            return null;
        }

        return tag;
    }

    /**
     * @param element should be a direct child tag of c:ProtectedStringType otherwise returns null
     */
    private XmlTag getProtectedStringTag(PsiElement element) {
        if (!(element instanceof XmlTag tag)) {
            return null;
        }

        XmlTag protectedString = tag.getParentTag();
        if (protectedString == null) {
            return null;
        }

        QName type = PsiUtils.getTagXsdType(protectedString);
        if (!Objects.equals(ProtectedStringType.COMPLEX_TYPE, type)) {
            return null;
        }

        return protectedString;
    }
}
