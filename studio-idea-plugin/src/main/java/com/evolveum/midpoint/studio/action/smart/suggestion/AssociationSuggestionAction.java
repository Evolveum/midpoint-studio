package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.studio.impl.MidPointClient;
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
    GenerateSuggestionDialogContext.ResourceDialogContextMode getModeDialogContext() {
        return GenerateSuggestionDialogContext.ResourceDialogContextMode.ASSOCIATION;
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
                                && objectList.get(0).getObjectType() != null
                                && !objectList.get(0).getObjectType().isEmpty())
                                ? objectList.get(0).getObjectType().get(0)
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
    List<SmartSuggestionObject<AssociationSuggestionType>> getSuggestions(
            MidPointClient client,
            GenerateSuggestionDialogContext generateSuggestionDialogContext
    ) {
        var associationSuggestion = client.getSuggestAssociations(
                generateSuggestionDialogContext.getResourceOid()
        );

        if (associationSuggestion == null) {
            return null;
        }

        return associationSuggestion.getAssociation().stream()
                .map(o -> new SmartSuggestionObject<>(o, getResources(generateSuggestionDialogContext)))
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
