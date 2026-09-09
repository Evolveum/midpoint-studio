/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsEditor;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsRenderer;
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
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.List;

public class AssociationSuggestionAction extends SmartSuggestionAction<AssociationSuggestionType> {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && associationAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDataModel.ResourceDialogContextMode.ASSOCIATION;
    }

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    SmartSuggestionTableModel<AssociationSuggestionType> getModel(Project project, PrismContext prismContext) {
        return new SmartSuggestionTableModel<>(List.of(
                new FilterableColumnInfo<>("Name", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        return ((AssociationSuggestionType) sso.getObject()).getDefinition().getDisplayName();
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Type of suggestion", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        // FIXME find out when is AI or system suggestion
                        return "System suggestion";
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Subject", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        var subjectList = ((AssociationSuggestionType) sso.getObject()).getDefinition().getSubject().getObjectType();
                        var subject = subjectList != null && !subjectList.isEmpty() ? subjectList.get(0) : null;
                        var subjectTypeDefinition = subject != null
                                ? ResourceTypeUtil.findObjectTypeDefinition(sso.getResource().asPrismObject(), subject.getKind(), subject.getIntent())
                                : null;
                        if (subjectTypeDefinition != null) {
                            return (subjectTypeDefinition.getDisplayName() + " - " +
                                    (subjectTypeDefinition.getDelineation().getObjectClass() != null ? subjectTypeDefinition.getDelineation().getObjectClass().getLocalPart() : " - "));
                        }
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Association data object", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        var association = ((AssociationSuggestionType) sso.getObject()).getDefinition().getAssociationObject();

                        if (association == null || association.getDelineation() == null || association.getDelineation().getObjectClass() == null) {
                            return " - ";
                        }
                        var associationObjectClass = association.getDelineation().getObjectClass();

                        return (association.getDisplayName() + " - " + (associationObjectClass != null ? associationObjectClass.getLocalPart() : " - "));
                    }
                    return null;
                }),
                new FilterableColumnInfo<>("Object", obj -> {
                    if (obj instanceof SmartSuggestionObject<?> sso) {
                        var objectList = ((AssociationSuggestionType) sso.getObject()).getDefinition().getObject();
                        var object = (objectList != null && !objectList.isEmpty()
                                && objectList.getFirst().getObjectType() != null
                                && !objectList.getFirst().getObjectType().isEmpty())
                                ? objectList.getFirst().getObjectType().getFirst()
                                : null;
                        var objectTypeDefinition = object != null
                                ? ResourceTypeUtil.findObjectTypeDefinition(sso.getResource().asPrismObject(), object.getKind(), object.getIntent())
                                : null;

                        if (objectTypeDefinition != null) {
                            return (objectTypeDefinition.getDisplayName() + " - " +
                                    (objectTypeDefinition.getDelineation().getObjectClass() != null ? objectTypeDefinition.getDelineation().getObjectClass().getLocalPart() : " - "));
                        }
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
        return client.submitOperationSuggestionAssociation(
                model.getResourceOid()
        );
    }

    @Override
    SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException {
        return client.getStatusInfoSuggestionAssociation(token);
    }

    @Override
    List<SmartSuggestionObject<AssociationSuggestionType>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel model
    ) {

        return result.getAssociationsSuggestionType().getAssociation().stream()
                .map(object ->
                        new SmartSuggestionObject<>(object, getResources(model)))
                .toList();
    }

    private boolean associationAllowed(PsiFile psiFile) {
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
