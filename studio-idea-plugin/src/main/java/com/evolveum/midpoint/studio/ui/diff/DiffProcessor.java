package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.common.cleanup.ObjectCleaner;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioCleanupListener;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class DiffProcessor<O extends ObjectType> {

    private static final Logger LOG = Logger.getInstance(DiffProcessor.class);

    public enum Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    private final Project project;

    private final DiffSource<O> leftSource;
    private final DiffSource<O> rightSource;

    private ObjectDelta<O> delta;

    private DiffPanel<O> panel;

    private Direction direction = Direction.RIGHT_TO_LEFT;

    private DiffStrategy strategy = DiffStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS; // todo use natural keys by default

    public DiffProcessor(@NotNull Project project, @NotNull DiffSource<O> left, @NotNull DiffSource<O> right) {
        this.project = project;

        this.leftSource = left;
        this.rightSource = right;

        panel = initPanel();
    }

    public Direction getDirection() {
        return direction;
    }

    public DiffSource<O> getLeftSource() {
        return leftSource;
    }

    public DiffSource<O> getRightSource() {
        return rightSource;
    }

    private DiffPanel<O> initPanel() {
        return new DiffPanel<>() {  // todo fix

            @Override
            protected JComponent createTextDiff() {
                return new JBLabel("todo implement"); // todo implement later
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
    }

    public void initialize() {
        try {
            recomputeDelta();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't parse object", ex);
        }
    }

    private void recomputeDelta() {
        DiffSource<O> targetSource = getTargetSource();

        PrismObject<O> target = targetSource.object();
        PrismObject<O> source = getSourceObject();

        delta = target.diff(source, strategy.getStrategy());

        panel.setTargetName(targetSource.name() + " (" + targetSource.type() + ")");
        panel.setDelta(delta);
    }

    public boolean hasChanges() {
        PrismObject<O> target = getTargetObject();
        PrismObject<O> source = getSourceObject();

        return target.equivalent(source);
    }

    public DiffSource<O> getTargetSource() {
        return direction == Direction.LEFT_TO_RIGHT ? rightSource : leftSource;
    }

    public DiffSource<O> getSourceSource() {
        return direction == Direction.LEFT_TO_RIGHT ? leftSource : rightSource;
    }

    public PrismObject<O> getTargetObject() {
        return getTargetSource().object();
    }

    public PrismObject<O> getSourceObject() {
        return getSourceSource().object();
    }

    private void onTreeSelectionChanged(List<DefaultMutableTreeNode> selected) {
        // todo implement
    }

    private List<AnAction> createToolbarActions() {
        List<AnAction> actions = new ArrayList<>();

        actions.add(new Separator());

        actions.add(new DiffStrategyComboAction(DiffStrategy.NATURAL_KEYS) {

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                diffStrategyChanged(selected);
            }
        });

        actions.add(new UiAction("Cleanup", AllIcons.General.InspectionsEye, e -> cleanupPerformed()));

        actions.add(new Separator());

        actions.add(new UiAction("Switch Sides", getDirectionIcon(), e -> switchSidesPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                e.getPresentation().setIcon(getDirectionIcon());
            }
        });

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

    private Icon getDirectionIcon() {
        return direction == Direction.LEFT_TO_RIGHT ? AllIcons.Diff.ArrowRight : AllIcons.Diff.Arrow;
    }

    public String getName() {
        return leftSource.getName();
    }

    public JComponent getComponent() {
        return panel;
    }

    private void diffStrategyChanged(@NotNull DiffStrategy strategy) {
        this.strategy = strategy;

        initialize();
    }

    protected void acceptPerformed() {
        try {
            List<DefaultMutableTreeNode> selected = panel.getSelectedNodes();

            PrismObject<O> target = getTargetObject();

            applyDeltaNodesToObject(target, selected);

            panel.removeNodes(selected);
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't apply delta", ex);
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
                delta.applyTo(object);
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
        CleanupService cs = CleanupService.get(project);

        MidPointClient client = null;
        Environment environment = EnvironmentService.getInstance(project).getSelected();
        if (environment != null) {
            client = new MidPointClient(project, environment, false, false);
        }

        ObjectCleaner processor = cs.createCleanupProcessor();
        processor.setListener(new StudioCleanupListener(project, client, MidPointUtils.DEFAULT_PRISM_CONTEXT));

        processor.process(leftSource.object());
        processor.process(rightSource.object());

        recomputeDelta();
    }

    private void switchSidesPerformed() {
        direction = direction == Direction.LEFT_TO_RIGHT ? Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT;

        recomputeDelta();
    }
}
