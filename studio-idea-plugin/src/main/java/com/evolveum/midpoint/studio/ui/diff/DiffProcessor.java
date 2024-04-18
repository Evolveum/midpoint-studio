package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
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
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DiffProcessor<O extends ObjectType> {

    private final Project project;

    private DiffSource leftSource;
    private DiffSource rightSource;

    private PrismObject<O> leftObject;
    private PrismObject<O> rightObject;

    private ObjectDelta<O> delta;

    private DiffPanel panel;

    private ParameterizedEquivalenceStrategy equivalenceStrategy = EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS_NATURAL_KEYS;

    public DiffProcessor(
            @NotNull Project project,
            @NotNull DiffSource left,
            @NotNull DiffSource right) {

        this.project = project;

        this.leftSource = left;
        this.rightSource = right;
    }

    public void initialize() {
        try {
            leftObject = parseObject(leftSource);
            rightObject = parseObject(rightSource);

            delta = leftObject.diff(rightObject, equivalenceStrategy);

            String leftTextContent = ClientUtils.serialize(MidPointUtils.DEFAULT_PRISM_CONTEXT, leftObject);
            String rightTextContent = ClientUtils.serialize(MidPointUtils.DEFAULT_PRISM_CONTEXT, rightObject);
            LightVirtualFile left = new LightVirtualFile(buildTextDiffFileName(leftSource, true), leftTextContent);
            LightVirtualFile right = new LightVirtualFile(buildTextDiffFileName(rightSource, false), rightTextContent);

            DiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, left, right);

            DiffRequestChain chain = new SimpleDiffRequestChain(request);

            CacheDiffRequestChainProcessor processor = new CacheDiffRequestChainProcessor(project, chain) {

                @Override
                protected @org.jetbrains.annotations.NotNull List<AnAction> getNavigationActions() {
                    List<AnAction> list = new ArrayList<>();
//                list.add(strategyCombo);
//                list.add(new Separator());

                    list.addAll(super.getNavigationActions());

                    return list;
                }
            };
            processor.updateRequest();

            panel = new DiffPanel(project, delta) {

                @Override
                protected JComponent createTextDiff() {
                    return processor.getComponent();
                }

                @Override
                protected @NotNull List<AnAction> createToolbarActions() {
                    return DiffProcessor.this.createToolbarActions();
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't parse object", ex);
        }
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


        actions.add(new UiAction("Cleanup right", AllIcons.General.InspectionsEye, e -> cleanupPerformed()));
//        group.add(new UiAction("Show text diff"));// todo implement

        actions.add(new Separator());

        actions.add(new UiAction("Save", AllIcons.General.GreenCheckmark, e -> savePerformed()));
        actions.add(new UiAction("Roll back", AllIcons.Actions.Rollback, e -> rollbackPerformed()));

        return actions;
    }

    private <O extends ObjectType> PrismObject<O> parseObject(DiffSource source) throws SchemaException, IOException {
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        try (InputStream is = source.file().getInputStream()) {
            PrismParser parser = ClientUtils.createParser(ctx, is);
            return parser.parse();
        }
    }

    private String buildTextDiffFileName(DiffSource source, boolean result) {
        String resultLabel = result ? ", result" : "";
        return source.getName() + " (" + source.type().name() + resultLabel + ")";
    }

    public String getName() {
        return leftSource.getName();
    }

    public JComponent getComponent() {
        return panel;
    }

    private void rollbackPerformed() {
        // todo implement
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

    private void acceptPerformed() {
        try {
            List<DefaultMutableTreeNode> selected = panel.getSelectedNodes();

            // todo implement - apply partial delta

            for (DefaultMutableTreeNode node : selected) {
                Object userObject = node.getUserObject();
                if (userObject instanceof ObjectDelta<?> od) {

                } else if (userObject instanceof ItemDelta<?, ?> id) {
                    ObjectDelta<O> delta = leftObject.createModifyDelta();
                    delta.addModification(id.clone());

                    delta.applyTo(leftObject);
                } else if (userObject instanceof DeltaItem) {

                }
            }

            panel.removeNodes(selected);
        } catch (Exception ex) {
            // todo fix
            ex.printStackTrace();
        }
    }

    private void ignorePerformed() {
        panel.removeNodes(panel.getSelectedNodes());
    }

    private void cleanupPerformed() {
        // todo implement
    }
}
