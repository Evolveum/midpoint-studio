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
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDialogContext;
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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.List;

public class CorrelationRuleSuggestionAction extends SmartSuggestionAction<ItemsSubCorrelatorType> {

    private final Logger log = Logger.getInstance(this.getClass());

    @Override
    boolean isLockable() {
        return false;
    }

    @Override
    boolean getPresentation(PsiFile psiFile) {
        return (getResourceOid() != null && correlationRuleAllowed(psiFile));
    }

    @Override
    GenerateSuggestionDialogContext.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDialogContext.ResourceDialogContextMode.CORRELATION;
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
    List<SmartSuggestionObject<ItemsSubCorrelatorType>> getSuggestions(
            MidPointClient client,
            GenerateSuggestionDialogContext generateSuggestionDialogContext
    ) {
        var correlationSuggestions = client.getSuggestCorrelationRule(
                generateSuggestionDialogContext.getResourceOid(),
                generateSuggestionDialogContext.getObjectType()
        );

        if (correlationSuggestions == null) {
            return null;
        }

        return correlationSuggestions.getSuggestion().stream()
                .flatMap(correlationSuggestionType ->
                        correlationSuggestionType.getCorrelation()
                                .getCorrelators()
                                .getItems()
                                .stream()
                                .map(o -> new SmartSuggestionObject<>(
                                                o,
                                                correlationSuggestionType,
                                                getResources(generateSuggestionDialogContext),
                                                generateSuggestionDialogContext.getObjectType()
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
