package com.evolveum.midpoint.studio.impl.lang.intention;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
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
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowAssociationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.lang.annotation.AnnotationHolder;
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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReplaceShadowRefAnnotatorIntention extends MidPointAnnotatorIntention {

    private static final String NAME = "Replace shadowRef";

    private static final String UNKNOWN_SHADOW_NAME = "Unknown";

    public ReplaceShadowRefAnnotatorIntention() {
        super(NAME);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        XmlTag expression = getExpression(element.getParent());
        if (expression == null) {
            return;
        }

        ThrowableRunnable<RuntimeException> task = () -> {
            List<XmlTag> shadowRefs = findShadowRefTags(expression);
            if (shadowRefs.isEmpty()) {
                return;
            }

            XmlTag associationTargetSearch = createTag(expression, SchemaConstantsGenerated.C_ASSOCIATION_TARGET_SEARCH);

            for (XmlTag shadowRef : shadowRefs) {
                String shadowOid = PsiUtils.getOidFromReferenceTag(shadowRef);
                String shadowName = getShadowName(project, shadowOid);

                XmlTag filter = createTag(associationTargetSearch, SearchObjectExpressionEvaluatorType.F_FILTER);
                createTag(
                        filter, SearchFilterType.F_TEXT, "\n" +
                                "// TODO please populate filter using shadow attributes that identify\n" +
                                "// shadow with oid=\"" + shadowOid + "\"\n" +
                                "// (" + shadowName + ")\n");
            }

            createTag(
                    associationTargetSearch, SearchObjectExpressionEvaluatorType.F_SEARCH_STRATEGY,
                    ObjectSearchStrategyType.ON_RESOURCE_IF_NEEDED.value());

            Arrays.stream(MidPointUtils.findSubTags(expression, SchemaConstantsGenerated.C_VALUE))
                    .forEach(XmlTag::delete);
        };

        WriteCommandAction.writeCommandAction(project)
                .withName(NAME)
                .withGroupId(NAME)
                .run(task);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        if (!(element instanceof XmlToken)) {
            return false;
        }

        if (!(element.getParent() instanceof XmlTag tag)) {
            return false;
        }

        if (!Objects.equals(SchemaConstantsGenerated.C_EXPRESSION, MidPointUtils.createQName(tag))) {
            return false;
        }

        return !findShadowRefTags(tag).isEmpty();
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

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        XmlTag expression = getExpression(element);
        if (expression == null) {
            return;
        }

        List<XmlTag> shadowRefs = findShadowRefTags(expression);
        if (shadowRefs.isEmpty()) {
            return;
        }

        createTagAnnotations(
                expression,
                holder,
                "This expression contains shadowRef element (" + shadowRefs.size() + ") that could represent " +
                        "possible problem when moving this midPoint object to another environment. Reason is " +
                        "reference to specific oid which would be different in another environment.");
    }

    private List<XmlTag> findShadowRefTags(XmlTag expression) {
        if (expression == null) {
            return List.of();
        }

        XmlTag[] values = MidPointUtils.findSubTags(expression, SchemaConstantsGenerated.C_VALUE);
        if (values.length == 0) {
            return List.of();
        }

        return Arrays.stream(values)
                .map(value -> MidPointUtils.findSubTags(value, ShadowAssociationType.F_SHADOW_REF))
                .flatMap(Arrays::stream)
                .toList();
    }

    private XmlTag getExpression(PsiElement element) {
        if (!(element instanceof XmlTag tag)) {
            return null;
        }

        if (!Objects.equals(SchemaConstantsGenerated.C_EXPRESSION, MidPointUtils.createQName(tag))) {
            return null;
        }

        return tag;
    }
}
