package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectSearchStrategyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SearchObjectExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Objects;

public class ReplaceShadowRefAnnotatorIntention extends PsiElementBaseIntentionAction implements Annotator {

    private static final String NAME = "Replace shadowRef";

    public static final QName SHADOW_REF = new QName(SchemaConstants.NS_C, "shadowRef");

    private static final String UNKNOWN_SHADOW_NAME = "Unknown";


    public ReplaceShadowRefAnnotatorIntention() {
        setText(NAME);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        XmlTag value = getValueExpressionTag(element);
        if (value == null) {
            return;
        }

        ThrowableRunnable<RuntimeException> task = () -> {
            XmlTag expression = value.getParentTag();
            if (expression == null) {
                return;
            }

            XmlTag shadowRef = getShadowRefTag(element);
            String shadowOid = PsiUtils.getOidFromReferenceTag(shadowRef);
            String shadowName = getShadowName(project, shadowOid);

            XmlTag associationTargetSearch = createTag(expression, SchemaConstantsGenerated.C_ASSOCIATION_TARGET_SEARCH);
            XmlTag filter = createTag(associationTargetSearch, SearchObjectExpressionEvaluatorType.F_FILTER);

            createTag(
                    filter, SearchFilterType.F_TEXT, "\n" +
                            "// TODO please populate filter using shadow attributes that identify\n" +
                            "// shadow with oid=\"" + shadowOid + "\"\n" +
                            "// (" + shadowName + ")\n");

            createTag(
                    associationTargetSearch, SearchObjectExpressionEvaluatorType.F_SEARCH_STRATEGY,
                    ObjectSearchStrategyType.ON_RESOURCE_IF_NEEDED.value());

            value.delete();
        };

        WriteCommandAction.writeCommandAction(project)
                .withName(NAME)
                .withGroupId(NAME)
                .run(task);
    }

    private XmlTag createTag(XmlTag parent, QName name) {
        return createTag(parent, name, null);
    }

    private XmlTag createTag(XmlTag parent, QName name, String body) {
        String prefix = parent.getPrefixByNamespace(name.getNamespaceURI());

        String tagPrefix = StringUtils.isBlank(prefix) ? "" : prefix + ":";
        String namespace = prefix == null ? name.getNamespaceURI() : null;

        XmlTag child = parent.createChildTag(tagPrefix + name.getLocalPart(), namespace, body, false);

        return parent.addSubTag(child, false);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        return getValueExpressionTag(element) != null;
    }

    private String getShadowName(Project project, String oid) {
        if (oid == null) {
            return UNKNOWN_SHADOW_NAME;
        }

        List<OidNameValue> items = ApplicationManager.getApplication().runReadAction(
                (Computable<List<OidNameValue>>) () ->
                        ObjectFileBasedIndexImpl.getOidNamesByOid(oid, project));
        if (!items.isEmpty()) {
            return items.get(0).getName();
        }

        EnvironmentService environmentService = EnvironmentService.getInstance(project);
        Environment environment = environmentService.getSelected();
        if (environment == null) {
            return UNKNOWN_SHADOW_NAME;
        }

        MidPointClient client = new MidPointClient(project, environment, true, true);
        MidPointObject object = client.get(ShadowType.class, oid, new SearchOptions().raw(true));
        if (object == null) {
            return UNKNOWN_SHADOW_NAME;
        }

        return object.getName();
    }

    private XmlTag getShadowRefTag(PsiElement element) {
        if (!(element instanceof XmlToken)) {
            return null;
        }

        if (!(element.getParent() instanceof XmlTag tag)) {
            return null;
        }

        if (!Objects.equals(SHADOW_REF, MidPointUtils.createQName(tag))) {
            return null;
        }

        return tag;
    }

    private XmlTag getValueExpressionTag(PsiElement element) {
        XmlTag shadowRef = getShadowRefTag(element);
        if (shadowRef == null) {
            return null;
        }

        XmlTag parent = shadowRef.getParentTag();

        return Objects.equals(SchemaConstantsGenerated.C_VALUE, MidPointUtils.createQName(parent)) ? parent : null;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        XmlTag shadowRef = getShadowRefTag(element);
        if (shadowRef == null) {
            return;
        }

        if (getValueExpressionTag(element) == null) {
            return;
        }

        String msg = "shadowRef element could represent possible problem when moving this midPoint " +
                "object to another environment. Reason is reference to specific oid which would " +
                "be different in another environment.";

        holder.newAnnotation(HighlightSeverity.WARNING, msg)
                .range(element.getTextRange())
                .tooltip(msg)
                .create();
    }
}
