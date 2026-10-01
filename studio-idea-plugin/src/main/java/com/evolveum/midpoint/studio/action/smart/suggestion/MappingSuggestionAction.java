/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.processor.ResourceObjectTypeIdentification;
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

public class MappingSuggestionAction extends SmartSuggestionAction<AttributeMappingsSuggestionType> {

    private final Logger log = Logger.getInstance(MappingSuggestionAction.class);

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && mappingAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDataModel.ResourceDialogContextMode.MAPPING;
    }

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    SmartSuggestionTableModel<AttributeMappingsSuggestionType> getModel(Project project, PrismContext prismContext) {
        return new SmartSuggestionTableModel<>(List.of(
                new FilterableColumnInfo<>("Name", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((AttributeMappingsSuggestionType) sso.getObject()).getDefinition().getInbound().get(0).getName();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("To resource attribute", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((AttributeMappingsSuggestionType) sso.getObject()).getDefinition().getRef();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Source", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((AttributeMappingsSuggestionType) sso.getObject()).getDefinition().getInbound().stream()
                                .findFirst()
                                .map(AbstractMappingType::getSource)
                                .flatMap(source -> source.stream().findFirst())
                                .map(VariableBindingDefinitionType::getPath)
                                .map(p -> p.getItemPath().toString())
                                .orElse("");
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

        var mappingsSuggestionWorkDefinitionType = new MappingsSuggestionWorkDefinitionType();
        mappingsSuggestionWorkDefinitionType.setResourceRef(resourceRef);
        ResourceObjectTypeIdentificationType resourceObjectTypeIdentificationType = new ResourceObjectTypeIdentificationType();
        resourceObjectTypeIdentificationType.setKind(dataModel.getObjectType().getKind());
        resourceObjectTypeIdentificationType.setIntent(dataModel.getObjectType().getIntent());
        mappingsSuggestionWorkDefinitionType.setObjectType(resourceObjectTypeIdentificationType);
        mappingsSuggestionWorkDefinitionType.setInbound(dataModel.getDirection().equals(GenerateSuggestionDataModel.Direction.INBOUND));
        mappingsSuggestionWorkDefinitionType.setForceRecomputeSchemaMatch(false);
        dataModel.getDataAccessPermissions().forEach(mappingsSuggestionWorkDefinitionType::permissions);

        String requestBodyContent = client.serialize(mappingsSuggestionWorkDefinitionType);

        return client.submitOperationSmartIntegration(
                RPCOperation.RPC_SUGGEST_MAPPINGS,
                requestBodyContent
        );
    }

    @Override
    SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException {

        return client.getStatusInfoSmartIntegration(
                RPCOperation.RPC_SUGGEST_MAPPINGS,
                token
        );
    }

    @Override
    List<SmartSuggestionObject<AttributeMappingsSuggestionType>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel dataModel
    ) {

        return result.getMappingsSuggestion().getAttributeMappings().stream()
                .map(o -> new SmartSuggestionObject<>(
                        o,
                        null,
                        getResources(dataModel),
                        dataModel.getObjectType()
                ))
                .toList();
    }

    private boolean mappingAllowed(PsiFile psiFile) {
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
