package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsEditor;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsRenderer;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.List;

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
    GenerateSuggestionDialogContext.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDialogContext.ResourceDialogContextMode.OBJECT_TYPE;
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
    List<SmartSuggestionObject<ResourceObjectTypeDefinitionType>> getSuggestions(
            MidPointClient client,
            GenerateSuggestionDialogContext generateSuggestionDialogContext
    ) {
        var objectSuggestion = client.getSuggestObjectTypes(
                generateSuggestionDialogContext.getResourceOid(),
                generateSuggestionDialogContext.getObjectClass()
        );

        if (objectSuggestion == null) {
            return null;
        }

        return objectSuggestion.getObjectType().stream()
                .map(o -> new SmartSuggestionObject<>(o, getResources(generateSuggestionDialogContext)))
                .toList();
    }
}
