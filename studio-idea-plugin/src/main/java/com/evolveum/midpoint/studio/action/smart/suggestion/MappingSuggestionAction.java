/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsEditor;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsRenderer;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class MappingSuggestionAction extends SmartSuggestionAction<AttributeMappingsSuggestionType> {

    private static final Logger log = Logger.getInstance(ObjectTypeSuggestionAction.class);

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && mappingAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDialogContext.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDialogContext.ResourceDialogContextMode.MAPPING;
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
                    @Override
                    public @Nullable Object valueOf(DefaultMutableTreeTableNode node) {
                        return node.getUserObject();
                    }

                    @Override
                    public boolean isCellEditable(DefaultMutableTreeTableNode node) {
                        return true;
                    }

                    @Override
                    public TableCellRenderer getCustomizedRenderer(DefaultMutableTreeTableNode node, TableCellRenderer renderer) {
                        return new ActionsRenderer();
                    }

                    @Override
                    public @NotNull TableCellRenderer getRenderer(DefaultMutableTreeTableNode o) {
                        return new ActionsRenderer();
                    }

                    @Override
                    public TableCellEditor getEditor(DefaultMutableTreeTableNode o) {
                        return new ActionsEditor(project, prismContext);
                    }
                }
        ));
    }

    @Override
    List<SmartSuggestionObject<AttributeMappingsSuggestionType>> getSuggestions(
            MidPointClient client,
            GenerateSuggestionDialogContext generateSuggestionDialogContext
    ) {
        var mappingsSuggestions = client.getSuggestMapping(
                generateSuggestionDialogContext.getResourceOid(),
                generateSuggestionDialogContext.getObjectType().getKind().value(),
                generateSuggestionDialogContext.getObjectType().getIntent(),
                generateSuggestionDialogContext.getDirection().equals(GenerateSuggestionDialogContext.Direction.INBOUND)
        );

        return mappingsSuggestions.getAttributeMappings().stream()
                .map(o -> new SmartSuggestionObject<>(
                        o,
                        null,
                        getResources(generateSuggestionDialogContext),
                        generateSuggestionDialogContext.getObjectType()
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
