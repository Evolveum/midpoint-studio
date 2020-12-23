package com.evolveum.midpoint.studio.ui.delta;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaObjectType;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.sun.istack.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectDeltaPanel extends BorderLayoutPanel {

    private enum DiffType {

        LOCAL,

        REMOTE

    }

    public enum DiffStrategy {

        DEFAULT("Default diff", ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION),

        EQUALS("Equals", ParameterizedEquivalenceStrategy.DEFAULT_FOR_EQUALS),

        IGNORE_METADATA("Ignore metadata", ParameterizedEquivalenceStrategy.IGNORE_METADATA);

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

    private CustomComboBoxAction<DiffStrategy> strategy;

    public ObjectDeltaPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        initLayout();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction showDelta = MidPointUtils.createAnAction("Show delta", AllIcons.Actions.Expandall, e -> {
        });
        group.add(showDelta);

        group.add(new Separator());

        AnAction localDiff = MidPointUtils.createAnAction("Local diff", AllIcons.Actions.StepOut, e -> showLocalDiff(e));
        group.add(localDiff);

        AnAction remoteDiff = MidPointUtils.createAnAction("Remote diff", AllIcons.Actions.TraceInto, e -> showRemoteDiff(e));
        group.add(remoteDiff);

        group.add(new Separator());

        strategy = new CustomComboBoxAction<>() {

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
        };
        group.add(strategy);

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("ObjectDeltaPanelToolbar", group, true);
        JComponent toolbar = actionToolbar.getComponent();
        add(toolbar, BorderLayout.NORTH);

        add(root, BorderLayout.CENTER);
    }

    private void showLocalDiff(AnActionEvent evt) {
        showDiff(evt, DiffType.LOCAL);
    }

    private void showRemoteDiff(AnActionEvent evt) {
        showDiff(evt, DiffType.REMOTE);
    }

    private void showDiff(AnActionEvent evt, DiffType diffType) {
        PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;

        DiffStrategy strategy = this.strategy.getSelected();
        ParameterizedEquivalenceStrategy pes = strategy != null ? strategy.getStrategy() : ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION;

        try (InputStream is = file.getInputStream()) {
            PrismParser parser = MidPointUtils.createParser(prismContext, is);

            ObjectDeltaObjectType odo = parser.parseRealValue(ObjectDeltaObjectType.class);
            PrismObject local = odo.getOldObject().asPrismObject();
            PrismObject remote = odo.getNewObject().asPrismObject();

            ObjectDelta delta;
            PrismObject o1 = null;
            PrismObject o2 = null;
            switch (diffType) {
                case LOCAL:
                    o1 = local;
                    delta = local.diff(remote, pes);

                    o2 = local.clone();
                    delta.applyTo(o2);
                    break;
                case REMOTE:
                    o2 = remote;
                    delta = remote.diff(local, pes);

                    o1 = remote.clone();
                    delta.applyTo(o1);
                    break;
            }

            LightVirtualFile file1 = new LightVirtualFile("Local.xml", MidPointUtils.serialize(prismContext, o1));
            LightVirtualFile file2 = new LightVirtualFile("Remote.xml", MidPointUtils.serialize(prismContext, o2));

            DiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, file1, file2);

            DiffRequestChain chain = new SimpleDiffRequestChain(request);
            CacheDiffRequestChainProcessor processor = new CacheDiffRequestChainProcessor(project, chain);

            // todo disposing stuff !!!
            root.add(processor.getComponent(), BorderLayout.CENTER);
            processor.updateRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
