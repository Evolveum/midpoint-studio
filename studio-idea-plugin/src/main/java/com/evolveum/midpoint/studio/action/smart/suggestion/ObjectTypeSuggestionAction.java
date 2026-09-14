/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.RPCOperation;
import com.evolveum.midpoint.studio.client.ServiceContext;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Dominik.
 */
public class ObjectTypeSuggestionAction extends SmartSuggestionAction<ResourceObjectTypeDefinitionType> {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(@NotNull PsiFile psiFile) {
        return getResourceOid() != null;
    }

    @Override
    GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDataModel.ResourceDialogContextMode.OBJECT_TYPE;
    }

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    SmartSuggestionTableModel<ResourceObjectTypeDefinitionType> getModel(Project project, PrismContext prismContext) {

        return new SmartSuggestionTableModel<>(List.of(
                new FilterableColumnInfo<>("Name",
                        obj -> {
                            if (obj instanceof SmartSuggestionObject<?> sso) {
                                return ((ResourceObjectTypeDefinitionType) sso.getObject()).getDisplayName();
                            }
                            return null;
                        }),
                new FilterableColumnInfo<>("Kind", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getKind().value();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Intent", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getIntent();
                    }
                    return null;
                }),
                new DefaultColumnInfo<>("Description", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getDescription();
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
            GenerateSuggestionDataModel model
    ) throws SchemaException, AuthenticationException, IOException {

        var objectTypesSuggestionWorkDefinitionType = new ObjectTypesSuggestionWorkDefinitionType();

        ObjectReferenceType resourceRef = new ObjectReferenceType();
        resourceRef.setOid(model.getResourceOid());
        objectTypesSuggestionWorkDefinitionType.setResourceRef(resourceRef);

        objectTypesSuggestionWorkDefinitionType.setObjectclass(model.getObjectClass().getObjectClassName());

        for(DataAccessPermissionType item : DataAccessPermissionType.values()) {
            objectTypesSuggestionWorkDefinitionType.permissions(item);
        }

        objectTypesSuggestionWorkDefinitionType.previousDelineation(null);

        String requestBodyContent = client.serialize(objectTypesSuggestionWorkDefinitionType);

        return client.submitOperationSmartIntegration(
                RPCOperation.RPC_SUGGEST_OBJECT_TYPES,
                requestBodyContent
        );
    }

    @Override
    SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException {

        return client.getStatusInfoSmartIntegration(
                RPCOperation.RPC_SUGGEST_OBJECT_TYPES,
                token
        );
    }

    @Override
    List<SmartSuggestionObject<ResourceObjectTypeDefinitionType>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel model
    ) {

        return result.getObjectTypesSuggestion().getObjectType().stream()
                .map(object ->
                        new SmartSuggestionObject<>(object, getResources(model))
                ).toList();
    }
}
