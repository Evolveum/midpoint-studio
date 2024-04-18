package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.studio.ui.delta.ObjectDeltaPanel;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiffPanel extends JBPanel {

    private final Project project;

    private ObjectDelta delta;

    public DiffPanel(@NotNull Project project, ObjectDelta delta) {
        super(new BorderLayout());

        this.project = project;
        this.delta = delta;

        initLayout();
    }

    private void initLayout() {
        JComponent mainToolbar = initMainToolbar(this);
        add(mainToolbar, BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, 0.5f);

        ObjectDeltaTree deltaTree = createDeltaTable();
        splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(deltaTree));

        JComponent diffEditor = createTextDiff();
        splitter.setSecondComponent(diffEditor);
        add(splitter, BorderLayout.CENTER);
    }

    private ObjectDeltaTree createDeltaTable() {
        ObjectDeltaTree tree = new ObjectDeltaTree(new ObjectDeltaTreeModel(delta));

        return tree;
    }

    private JComponent createTextDiff() {
        LightVirtualFile left = new LightVirtualFile("left.xml", "left\ncontent");
        LightVirtualFile right = new LightVirtualFile("right.xml", "right\ncontent");

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

        return processor.getComponent();
    }

    private JComponent initMainToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();

        CustomComboBoxAction<ObjectDeltaPanel.DiffStrategy> strategyCombo = new CustomComboBoxAction<>() {

            @Override
            public ObjectDeltaPanel.DiffStrategy getDefaultItem() {
                return ObjectDeltaPanel.DiffStrategy.DEFAULT;
            }

            @Override
            public List<ObjectDeltaPanel.DiffStrategy> getItems() {
                return Arrays.asList(ObjectDeltaPanel.DiffStrategy.values());
            }

            @Override
            protected String createItemLabel(ObjectDeltaPanel.DiffStrategy item) {
                return item != null ? item.getLabel() : "";
            }

            @Override
            public void setSelected(ObjectDeltaPanel.DiffStrategy selected) {
                super.setSelected(selected);

                // todo change was done
            }
        };
        group.add(strategyCombo);

        group.add(new Separator());

        group.add(new UiAction("Apply to left", e -> {
        }));
        group.add(new UiAction("Apply to right", e -> {
        }));

        group.add(new Separator());

        group.add(new UiAction("Cleanup left", e -> {
        }));
        group.add(new UiAction("Cleanup right", e -> {
        }));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("toolbar", group, true);
        toolbar.setTargetComponent(parent);

        return toolbar.getComponent();
    }
}
