/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.RPCOperation;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.io.IOException;
import java.util.List;

public class CorrelationRuleSuggestionAction extends SmartSuggestionAction<ItemsSubCorrelatorType> {

    private final Logger log = Logger.getInstance(CorrelationRuleSuggestionAction.class);

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && correlationRuleAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDataModel.ResourceDialogContextMode.CORRELATION;
    }

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    SmartSuggestionTableModel<ItemsSubCorrelatorType> getModel(Project project, PrismContext prismContext) {
        return new SmartSuggestionTableModel<>(List.of(
                new FilterableColumnInfo<>("Name", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ItemsSubCorrelatorType) sso.getObject()).getName();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Weight", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ItemsSubCorrelatorType) sso.getObject()).getComposition().getWeight();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Tier", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ItemsSubCorrelatorType) sso.getObject()).getComposition().getTier();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Efficiency", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        if (sso.getParent() instanceof CorrelationSuggestionType correlationSuggestionType) {
                            Double quality = correlationSuggestionType.getQuality();
                            return (quality != null && quality != -1) ? String.valueOf((quality * 100)) : "-";
                        }
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Description", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ItemsSubCorrelatorType) sso.getObject()).getDescription();
                    }
                    return null;
                }),
                new DefaultColumnInfo<>("Activities") {
                }
        ));
    }

    @Override
    String submitOperation(
            MidPointClient client,
            GenerateSuggestionDataModel dataModel
    ) throws SchemaException, AuthenticationException, IOException {

        ObjectReferenceType resourceRef = new ObjectReferenceType();
        resourceRef.setOid(dataModel.getResourceOid());

        var correlationSuggestionWorkDefinitionType = new CorrelationSuggestionWorkDefinitionType();
        correlationSuggestionWorkDefinitionType.setResourceRef(resourceRef);
        ResourceObjectTypeIdentificationType resourceObjectTypeIdentificationType = new ResourceObjectTypeIdentificationType();
        resourceObjectTypeIdentificationType.setKind(dataModel.getObjectType().getKind());
        resourceObjectTypeIdentificationType.setIntent(dataModel.getObjectType().getIntent());
        correlationSuggestionWorkDefinitionType.setObjectType(resourceObjectTypeIdentificationType);
        correlationSuggestionWorkDefinitionType.setForceRecomputeSchemaMatch(false);
        dataModel.getDataAccessPermissions().forEach(correlationSuggestionWorkDefinitionType::permissions);

        String requestBodyContent = client.serialize(correlationSuggestionWorkDefinitionType);

        return client.submitOperationSmartIntegration(
                RPCOperation.RPC_SUGGEST_CORRELATIONS,
                requestBodyContent
        );
    }

    @Override
    SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException {

        return client.getStatusInfoSmartIntegration(
                RPCOperation.RPC_SUGGEST_CORRELATIONS,
                token
        );
    }

    @Override
    List<SmartSuggestionObject<ItemsSubCorrelatorType>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel dataModel
    ) {

        return result.getCorrelationSuggestions().getSuggestion().stream()
                .flatMap(correlationSuggestionType ->
                        correlationSuggestionType.getCorrelation()
                                .getCorrelators()
                                .getItems()
                                .stream()
                                .map(object ->
                                     new SmartSuggestionObject<>(
                                             object,
                                             correlationSuggestionType,
                                             getResources(dataModel),
                                             dataModel.getObjectType()
                                     )
                                )
                )
                .toList();
    }

    private boolean correlationRuleAllowed(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }

        XmlTag root = ((XmlFile) psiFile).getRootTag();
        if (root == null) {
            return false;
        }

        XmlTag[] schemaHandlingTags = root.findSubTags("schemaHandling");
        if (schemaHandlingTags.length == 0) {
            return false;
        }

        for (XmlTag schema : schemaHandlingTags) {
            if (schema.findFirstSubTag("objectType") != null) {
                return true;
            }
        }

        return false;
    }
}
