package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.common.cleanup.ObjectCleaner;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.ui.ToolbarAction;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiffProcessor<O extends ObjectType> {

    private static final Logger LOG = Logger.getInstance(DiffProcessor.class);

    public enum Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    private final Project project;

    private String id;

    private final DiffSource<O> leftSource;
    private final DiffSource<O> rightSource;

    private final DiffPanel<O> diffPanel;

    private final SimpleDiffPanel<O> simpleDiffPanel;

    private ObjectDelta<O> delta;

    private final List<ApplicableDelta<?>> leftIgnoredDelta;

    private final List<ApplicableDelta<?>> rightIgnoredDelta;

    private Direction direction = Direction.RIGHT_TO_LEFT;

    private DiffStrategy strategy = DiffStrategy.NATURAL_KEYS;

    private DiffStrategyComboAction strategyAction;

    public DiffProcessor(
            @NotNull Project project, @Nullable String id, @NotNull DiffSource<O> left, @NotNull DiffSource<O> right,
            @NotNull List<ApplicableDelta<?>> leftIgnoredDelta, @NotNull List<ApplicableDelta<?>> rightIgnoredDelta) {

        this.project = project;

        this.id = id;

        this.leftSource = left;
        this.rightSource = right;

        this.leftIgnoredDelta = leftIgnoredDelta;
        this.rightIgnoredDelta = rightIgnoredDelta;

        diffPanel = initDiffPanel();
        simpleDiffPanel = initSimpleDiffPanel();
    }

    public String getId() {
        return id;
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

    public DiffStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(DiffStrategy strategy) {
        this.strategy = strategy;

        computeDelta();
    }

    private DiffPanel<O> initDiffPanel() {
        return new DiffPanel<>(project, this) {

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

    private SimpleDiffPanel<O> initSimpleDiffPanel() {
        return new SimpleDiffPanel<>(project, this);
    }

    public void computeDelta() {
        try {
            DiffSource<O> targetSource = getTarget();

            PrismObject<O> target = targetSource.object();
            PrismObject<O> source = getSourceObject();

            if (target == null || source == null) {
                Class<O> type = target != null ? target.getCompileTimeClass() : source.getCompileTimeClass();
                delta = PrismContext.get().deltaFactory().object().create(type, ChangeType.MODIFY);
            } else {
                delta = target.diff(source, strategy.getStrategy());
            }

            diffPanel.setTargetName(targetSource.name() + " (" + targetSource.type() + ")");
            diffPanel.setDelta(target, delta);

            diffPanel.refreshInternalDiffRequestProcessor();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't parse object", ex);
        }
    }

    public boolean hasChanges() {
        PrismObject<O> target = getTargetObject();
        PrismObject<O> source = getSourceObject();

        return target.equivalent(source);
    }

    /**
     * @return Returns {@link DiffSource} that represents target object based on current direction.
     * Diff is always computed for specific direction.
     */
    public DiffSource<O> getTarget() {
        return direction == Direction.LEFT_TO_RIGHT ? rightSource : leftSource;
    }

    /**
     * @return Returns {@link DiffSource} that represents target object based on current direction.
     * Diff is always computed for specific direction.
     */
    public DiffSource<O> getSource() {
        return direction == Direction.LEFT_TO_RIGHT ? leftSource : rightSource;
    }

    public PrismObject<O> getTargetObject() {
        return getTarget().object();
    }

    public List<ApplicableDelta<?>> getTargetIgnoredDelta() {
        return direction == Direction.LEFT_TO_RIGHT ? rightIgnoredDelta : leftIgnoredDelta;
    }

    public PrismObject<O> getSourceObject() {
        return getSource().object();
    }

    private void onTreeSelectionChanged(List<DefaultMutableTreeNode> selected) {
        if (selected.isEmpty()) {
            diffPanel.refreshInternalDiffRequestProcessor();
            return;
        }

        DefaultMutableTreeNode node = selected.get(0);
        Object userObject = node.getUserObject();

        String before = "";
        String after = "";
        try {
            if (userObject instanceof ItemDeltaValueTreeNode idvtn) {
                String content =PrismContext.get().xmlSerializer().serialize(idvtn.getValue());
                switch (idvtn.getModificationType()) {
                    case ADD, REPLACE -> after = content;
                    case DELETE -> before = content;
                }
            } else if (userObject instanceof ItemDeltaTreeNode idt) {
                Item<?, ?> targetItem = idt.getTargetItem() != null ?
                        idt.getTargetItem().clone() : idt.getValue().getDefinition().instantiate();

                before = serializeItem(targetItem);

                ItemDelta<?, ?> delta = idt.getValue().cloneWithChangedParentPath(ItemPath.EMPTY_PATH);

                Item item = targetItem.clone();
                delta.applyTo(item);

                after = serializeItem(item);
            }
        } catch (Exception ex) {
            LOG.debug("Couldn't serialize prism value", ex);
        }

        diffPanel.refreshInternalDiffRequestProcessor(before, after);
    }

    private String serializeItem(Item<?, ?> item) throws SchemaException {
        if (item == null || item.getValues() == null) {
            return "";
        }
        List<PrismValue> values = (List) item.getValues();
        PrismSerializer<String> serializer = PrismContext.get().xmlSerializer();

        StringBuilder sb = new StringBuilder();
        for (PrismValue value : values) {
            sb.append(serializer.serialize(value));
        }

        return sb.toString();
    }

    private List<AnAction> createToolbarActions() {
        List<AnAction> actions = new ArrayList<>();

        actions.add(new Separator());

        strategyAction = new DiffStrategyComboAction(strategy) {

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                diffStrategyChanged(selected);
            }

            @Override
            public DiffStrategy getSelected() {
                return strategy;
            }
        };
        actions.add(strategyAction);

        actions.add(new ToolbarAction("Cleanup", AllIcons.Actions.ToggleVisibility, e -> cleanupPerformed()));

        actions.add(new Separator());

        actions.add(new ToolbarAction("Switch Sides", getDirectionIcon(), e -> switchSidesPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                e.getPresentation().setIcon(getDirectionIcon());
            }
        });

        actions.add(new ToolbarAction("Accept", AllIcons.RunConfigurations.ShowPassed, e -> acceptPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = !diffPanel.getSelectedNodes().isEmpty();
                e.getPresentation().setEnabled(enabled);
            }
        });
        actions.add(new ToolbarAction("Ignore", AllIcons.RunConfigurations.ShowIgnored, e -> ignorePerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = !diffPanel.getSelectedNodes().isEmpty();
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

    public JComponent getDiffComponent() {
        return diffPanel;
    }

    public JComponent getSimpleDiffComponent() {
        return simpleDiffPanel;
    }

    private void diffStrategyChanged(@NotNull DiffStrategy strategy) {
        this.strategy = strategy;

        computeDelta();

        simpleDiffPanel.refreshInternalDiffRequestProcessor();
    }

    protected void acceptPerformed() {
        try {
            List<DefaultMutableTreeNode> selected = diffPanel.getSelectedNodes();

            PrismObject<O> target = getTargetObject();

            applyDeltaNodesToObject(target, selected);

            diffPanel.removeNodes(selected);

            simpleDiffPanel.refreshInternalDiffRequestProcessor();
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

            Object userObject = node.getUserObject();
            if (!(userObject instanceof ObjectDeltaTreeNode odt)) {
                continue;
            }

            ObjectDelta<O> delta = odt.getApplicableDelta().getDelta(object);
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
        try {
            List<DefaultMutableTreeNode> selected = diffPanel.getSelectedNodes();

            for (DefaultMutableTreeNode node : selected) {
                if (hasParentSelected(node, selected)) {
                    continue;
                }

                // todo somehow update synchronization object item - add ignored items
                //  add equals/hashcode do applicable delta classes

                if (!(node.getUserObject() instanceof ObjectDeltaTreeNode<?> odt)) {
                    continue;
                }

                getTargetIgnoredDelta().add(odt.getApplicableDelta());
            }

            diffPanel.removeNodes(selected);
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't apply delta", ex);
        }
    }

    private void cleanupPerformed() {
        CleanupService cs = CleanupService.get(project);

        ObjectCleaner processor = cs.createCleanupProcessor();
        processor.process(leftSource.object());
        processor.process(rightSource.object());

        computeDelta();
    }

    private void switchSidesPerformed() {
        direction = direction == Direction.LEFT_TO_RIGHT ? Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT;

        computeDelta();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffProcessor<?> processor = (DiffProcessor<?>) o;
        return Objects.equals(project, processor.project) && Objects.equals(leftSource, processor.leftSource) && Objects.equals(rightSource, processor.rightSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, leftSource, rightSource);
    }
}
