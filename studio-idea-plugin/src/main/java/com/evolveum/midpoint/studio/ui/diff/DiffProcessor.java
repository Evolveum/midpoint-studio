package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DiffProcessor<O extends ObjectType> {

    private final Project project;

    private final DiffSource leftSource;
    private final DiffSource rightSource;

    private PrismObject<O> leftObject;
    private PrismObject<O> rightObject;

    private ObjectDelta<O> delta;

    private DiffPanel<O> panel;

    // todo fix EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS_NATURAL_KEYS;
    private ParameterizedEquivalenceStrategy equivalenceStrategy = EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS;

    public DiffProcessor(
            @NotNull Project project,
            @NotNull DiffSource left,
            @NotNull DiffSource right) {

        this.project = project;

        this.leftSource = left;
        this.rightSource = right;
    }

    public PrismObject<O> getLeftObject() {
        return leftObject;
    }

    public PrismObject<O> getRightObject() {
        return rightObject;
    }

    public void initialize() {
        try {
            leftObject = parseObject(leftSource);
            rightObject = parseObject(rightSource);

            delta = leftObject.diff(rightObject, equivalenceStrategy);

            panel = new DiffPanel<>(project, leftSource.file().getName() + " (" + leftSource.type() + ")", delta) {  // todo fix

                @Override
                protected JComponent createTextDiff() {
                    return new JPanel(); // todo implement later
                }

                @Override
                protected @NotNull List<AnAction> createToolbarActions() {
                    return DiffProcessor.this.createToolbarActions();
                }

                @Override
                protected void onTreeSelectionChanged(@NotNull List<DefaultMutableTreeNode> selected) {
                    DiffProcessor.this.onTreeSelectionChanged(selected);
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't parse object", ex);
        }
    }

    private void onTreeSelectionChanged(List<DefaultMutableTreeNode> selected) {
        // todo implement
    }

    private List<AnAction> createToolbarActions() {
        List<AnAction> actions = new ArrayList<>();

        actions.add(new Separator());

        actions.add(new DiffStrategyComboAction(DiffStrategy.NATURAL_KEYS) {

            @Override
            public DiffStrategy getDefaultItem() {
                return super.getDefaultItem();
            }

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                diffStrategyChanged(selected);
            }
        });

        actions.add(new Separator());

        actions.add(new UiAction("Cleanup right", AllIcons.General.InspectionsEye, e -> cleanupPerformed()));

        actions.add(new UiAction("Accept", AllIcons.RunConfigurations.ShowPassed, e -> acceptPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = !panel.getSelectedNodes().isEmpty();
                e.getPresentation().setEnabled(enabled);
            }
        });
        actions.add(new UiAction("Ignore", AllIcons.RunConfigurations.ShowIgnored, e -> ignorePerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = !panel.getSelectedNodes().isEmpty();
                e.getPresentation().setEnabled(enabled);
            }
        });

        return actions;
    }

    private <T extends ObjectType> PrismObject<T> parseObject(DiffSource source) throws SchemaException, IOException {
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        try (InputStream is = source.file().getInputStream()) {
            PrismParser parser = ClientUtils.createParser(ctx, is);
            return parser.parse();
        }
    }

    private String buildTextDiffFileName(DiffSource source, boolean result) {
        String resultLabel = result ? "Result: " : "Source: ";

        return resultLabel + source.file().getName() + " (" + source.type() + ")";
    }

    public String getName() {
        return leftSource.getName();
    }

    public JComponent getComponent() {
        return panel;
    }

    private void savePerformed() {
        RunnableUtils.runWriteAction(() -> {
            try {
                String xml = ClientUtils.serialize(MidPointUtils.DEFAULT_PRISM_CONTEXT, leftObject);
                leftSource.file().setBinaryContent(xml.getBytes());
            } catch (Exception ex) {
                // todo fix
                ex.printStackTrace();
            }
        });
    }

    private void diffStrategyChanged(DiffStrategy strategy) {
        // todo implement
    }

    protected void acceptPerformed() {
        try {
            List<DefaultMutableTreeNode> selected = panel.getSelectedNodes();

            // todo implement - apply partial delta
            applyDeltaNodesToObject(leftObject, selected);

            panel.removeNodes(selected);
        } catch (Exception ex) {
            // todo fix
            ex.printStackTrace();
        }
    }

    private void applyDeltaNodesToObject(PrismObject<O> object, List<DefaultMutableTreeNode> selected)
            throws SchemaException {

        for (DefaultMutableTreeNode node : selected) {
            if (hasParentSelected(node, selected)) {
                continue;
            }

            ObjectDelta<O> delta = null;

            Object userObject = node.getUserObject();
            if (userObject instanceof ObjectDelta<?> od) {
                delta = (ObjectDelta<O>) od;
            } else if (userObject instanceof ItemDelta<?, ?> id) {
                delta = object.createModifyDelta();
                delta.addModification(id.clone());
            } else if (userObject instanceof DeltaItem di) {
                PrismValue cloned = di.value().clone();

                ItemDelta itemDelta = di.parent().clone();
                itemDelta.clear();
                switch (di.modificationType()) {
                    case REPLACE -> itemDelta.addValueToReplace(cloned);
                    case ADD -> itemDelta.addValueToAdd(cloned);
                    case DELETE -> itemDelta.addValueToDelete(cloned);
                }

                delta = object.createModifyDelta();
                delta.addModification(itemDelta);
            }

            if (delta != null) {
                delta.applyTo(leftObject);
            }
        }
    }

    private boolean hasParentSelected(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> selected) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return false;
        }

        if (selected.contains(parent)) {
            return true;
        }

        return hasParentSelected(parent, selected);
    }

    private void ignorePerformed() {
        panel.removeNodes(panel.getSelectedNodes());
    }

    private void cleanupPerformed() {
        // todo implement
    }
}
