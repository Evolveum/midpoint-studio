package com.evolveum.midpoint.studio.ui.delta;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.impl.xml.DiffObjectType;
import com.evolveum.midpoint.studio.impl.xml.DiffType;
import com.evolveum.midpoint.studio.impl.xml.ObjectsDiffFactory;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.ui.diff.DiffStrategy;
import com.evolveum.midpoint.studio.ui.diff.DiffStrategyComboAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaObjectType;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.sun.istack.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectDeltaPanel extends BorderLayoutPanel implements Disposable {

    private static final Logger LOG = Logger.getInstance(ObjectDeltaPanel.class);

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
        strategyCombo = new DiffStrategyComboAction(DiffStrategy.DEFAULT) {

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                refreshDiffView();
            }
        };

        add(root, BorderLayout.CENTER);
    }

    private void refreshDiffView() {
        new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;

                DiffStrategy strategy = ObjectDeltaPanel.this.strategyCombo.getSelected();
                ParameterizedEquivalenceStrategy pes = strategy != null ? strategy.getStrategy() : ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION;

                try {
                    DiffType diff = parseDiff(prismContext);

                    DiffObjectType first = diff.getFirstObject();
                    DiffObjectType second = diff.getSecondObject();

                    if (first == null || first.getObject() == null) {
                        LOG.debug("First object not defined in DiffObjectType");
                        return;
                    }

                    if (second == null) {
                        LOG.debug("Second object not defined in DiffObjectType");
                        return;
                    }

                    ObjectType firstObject = first.getObject();
                    ObjectType secondObject = second.getObject();

                    PrismObject local = firstObject.asPrismObject();
                    PrismObject remote = secondObject != null ? secondObject.asPrismObject() : null;

                    PrismObject o1 = local;
                    PrismObject o2 = local.clone();

                    ObjectDelta delta = local.diff(remote, pes);
                    delta.applyTo(o2);

                    String firstFile = buildFileName(first, "First.xml");
                    String secondFile = buildFileName(second, "Second.xml");

                    LightVirtualFile file1 = new LightVirtualFile(firstFile, ClientUtils.serialize(prismContext, o1));
                    LightVirtualFile file2 = new LightVirtualFile(secondFile, ClientUtils.serialize(prismContext, o2));

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
                    MidPointUtils.publishExceptionNotification(project, null, ObjectDeltaPanel.class, ObjectDeltaEditor.NOTIFICATION_KEY,
                            "Couldn't create diff, reason: " + ex.getMessage(), ex);
                }
            }
        }.run();
    }

    private String buildFileName(DiffObjectType dot, String defaultName) {
        if (dot == null) {
            return defaultName;
        }

        if (dot.getFileName() != null) {
            return dot.getFileName();
        }

        if (dot.getLocation() != null) {
            return dot.getLocation().getValue() + ".xml";
        }

        return defaultName;
    }


    private DiffType parseDiff(PrismContext prismContext) throws IOException, SchemaException {
        String data = VfsUtil.loadText(file);

        DiffType diff = new ObjectsDiffFactory(prismContext).parseObjectsDiff(data);
        if (diff != null) {
            return diff;
        }

        PrismParser parser = ClientUtils.createParser(prismContext, data);
        try {
            ObjectDeltaObjectType odo = parser.parseRealValue(ObjectDeltaObjectType.class);

            diff = new DiffType();
            diff.setFirstObject(createDiffObject(odo.getOldObject()));
            diff.setSecondObject(createDiffObject(odo.getNewObject()));
        } catch (Exception ignored) {
            // intentionally ignored, this is here only for backward compatibility - when diff data was stored as ObjectDeltaObjectType
        }

        return diff;
    }

    private DiffObjectType createDiffObject(com.evolveum.prism.xml.ns._public.types_3.ObjectType obj) {
        DiffObjectType dot = new DiffObjectType();
        dot.setObject((ObjectType) obj.asPrismObject().asObjectable());

        return dot;
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
