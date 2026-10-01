/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractSmartIntegrationOperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AttributeMappingsSuggestionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.io.IOException;
import java.util.List;

public class FocusTypeSuggestionAction extends SmartSuggestionAction<AttributeMappingsSuggestionType> {

    private final Logger log = Logger.getInstance(FocusTypeSuggestionAction.class);

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && focusTypeAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext() {
        return null;
    }

    @Override
    SmartSuggestionTableModel<AttributeMappingsSuggestionType> getModel(Project project, PrismContext prismContext) {
        return null;
    }

    @Override
    String submitOperation(MidPointClient client, GenerateSuggestionDataModel dataModel) throws SchemaException, AuthenticationException, IOException {
        return "";
    }

    @Override
    SmartIntegrationOperationStatusInfoType getStatusInfo(MidPointClient client, String token) throws SchemaException, AuthenticationException, IOException {
        return null;
    }

    @Override
    List<SmartSuggestionObject<AttributeMappingsSuggestionType>> getResultSuggestions(AbstractSmartIntegrationOperationResultType result, GenerateSuggestionDataModel dataModel) throws SchemaException {
        // TODO processing the result for FocusType – generating an smart suggestion
        return null;
    }

    private boolean focusTypeAllowed(PsiFile psiFile) {
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
