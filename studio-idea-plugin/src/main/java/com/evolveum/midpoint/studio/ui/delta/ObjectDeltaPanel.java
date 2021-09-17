package com.evolveum.midpoint.studio.ui.delta;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaObjectType;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.sun.istack.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectDeltaPanel extends BorderLayoutPanel implements Disposable {

    public enum DiffStrategy {

        LITERAL("Literal", ParameterizedEquivalenceStrategy.LITERAL),

        DATA("Data", ParameterizedEquivalenceStrategy.DATA),

        IGNORE_METADATA("Ignore Metadata", ParameterizedEquivalenceStrategy.IGNORE_METADATA),

        DEFAULT("Default Compare", ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION),

        REAL_VALUE("Real Value", ParameterizedEquivalenceStrategy.REAL_VALUE);

        private String label;

        private ParameterizedEquivalenceStrategy strategy;

        DiffStrategy(String label, ParameterizedEquivalenceStrategy strategy) {
            this.label = label;
            this.strategy = strategy;
        }

        public String getLabel() {
            return label;
        }

        public ParameterizedEquivalenceStrategy getStrategy() {
            return strategy;
        }
    }

    private Project project;

    private VirtualFile file;

    private JPanel root = new BorderLayoutPanel();

    private CustomComboBoxAction<DiffStrategy> strategyCombo;

    private CacheDiffRequestChainProcessor processor;

    public ObjectDeltaPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        initLayout();

        refreshDiffView();
    }

    private void initLayout() {
        strategyCombo = new CustomComboBoxAction<>() {

            @Override
            public DiffStrategy getDefaultItem() {
                return DiffStrategy.DEFAULT;
            }

            @Override
            public List<DiffStrategy> getItems() {
                return Arrays.asList(DiffStrategy.values());
            }

            @Override
            protected String createItemLabel(DiffStrategy item) {
                return item != null ? item.getLabel() : "";
            }

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                refreshDiffView();
            }
        };

        add(root, BorderLayout.CENTER);
    }

    private void refreshDiffView() {
        PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;

        DiffStrategy strategy = this.strategyCombo.getSelected();
        ParameterizedEquivalenceStrategy pes = strategy != null ? strategy.getStrategy() : ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION;

        try (InputStream is = file.getInputStream()) {
            PrismParser parser = ClientUtils.createParser(prismContext, is);

            ObjectDeltaObjectType odo = parser.parseRealValue(ObjectDeltaObjectType.class);
            PrismObject local = odo.getOldObject().asPrismObject();
            PrismObject remote = odo.getNewObject().asPrismObject();

            PrismObject o1 = local;
            PrismObject o2 = local.clone();

            ObjectDelta delta = local.diff(remote, pes);
            delta.applyTo(o2);

            LightVirtualFile file1 = new LightVirtualFile("Local.xml", ClientUtils.serialize(prismContext, o1));
            LightVirtualFile file2 = new LightVirtualFile("Remote.xml", ClientUtils.serialize(prismContext, o2));

            DiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, file1, file2);

            DiffRequestChain chain = new SimpleDiffRequestChain(request);

            cleanupDiffView();

            processor = new CacheDiffRequestChainProcessor(project, chain) {

                @Override
                protected @org.jetbrains.annotations.NotNull List<AnAction> getNavigationActions() {
                    List<AnAction> list = new ArrayList<>();
                    list.add(strategyCombo);
                    list.add(new Separator());

                    list.addAll(super.getNavigationActions());

                    return list;
                }
            };
            root.add(processor.getComponent(), BorderLayout.CENTER);
            processor.updateRequest();
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(null, ObjectDeltaPanel.class, ObjectDeltaEditor.NOTIFICATION_KEY,
                    "Couldn't create diff, reason: " + ex.getMessage(), ex);
        }
    }

    private void cleanupDiffView() {
        if (processor != null) {
            processor.dispose();
            processor = null;
        }

        if (root.getComponentCount() > 0) {
            root.removeAll();
        }
    }

    @Override
    public void dispose() {
        cleanupDiffView();
    }
}
