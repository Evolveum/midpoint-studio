package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.ui.delta.ObjectDeltaEditor;
import com.evolveum.midpoint.studio.ui.delta.ObjectDeltaPanel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleDiffPanel<O extends ObjectType> extends BorderLayoutPanel implements Disposable {

    private static final Logger LOG = Logger.getInstance(SimpleDiffPanel.class);

    private final Project project;

    private final CustomComboBoxAction<DiffStrategy> strategyAction;

    private final DiffProcessor<O> processor;

    private SimpleDiffRequestProcessor diffRequestProcessor;

    public SimpleDiffPanel(@NotNull Project project, @NotNull DiffProcessor<O> processor) {
        this.project = project;
        this.processor = processor;

        strategyAction = new DiffStrategyComboAction(processor.getStrategy()) {

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                processor.setStrategy(selected);

                refreshInternalDiffRequestProcessor();
            }

            @Override
            public DiffStrategy getSelected() {
                return processor.getStrategy();
            }
        };

        refreshInternalDiffRequestProcessor();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    public void refreshInternalDiffRequestProcessor() {
        try {
            removeAll();

            if (diffRequestProcessor != null) {
                diffRequestProcessor.dispose();
            }

            DiffSource<O> leftSource = processor.getLeftSource();
            DiffSource<O> rightSource = processor.getRightSource();

            PrismObject<O> left = leftSource.object();
            PrismObject<O> right = rightSource.object();

            ObjectDelta<O> delta = left.diff(right, strategyAction.getSelected().getStrategy());

            PrismObject<O> o1 = left.clone();
            PrismObject<O> o2 = left.clone();

            delta.applyTo(o2);

            String leftContent = ClientUtils.serialize(MidPointUtils.DEFAULT_PRISM_CONTEXT, o1);
            String rightContent = ClientUtils.serialize(MidPointUtils.DEFAULT_PRISM_CONTEXT, o2);

            LightVirtualFile leftFile = new LightVirtualFile(
                    leftSource.getFullName(), XmlFileType.INSTANCE, leftContent, System.currentTimeMillis());

            LightVirtualFile rightFile = new LightVirtualFile(
                    rightSource.getFullName(), XmlFileType.INSTANCE, rightContent, System.currentTimeMillis());

            diffRequestProcessor = new SimpleDiffRequestProcessor(project) {

                @Override
                protected @org.jetbrains.annotations.NotNull List<AnAction> getNavigationActions() {
                    List<AnAction> list = new ArrayList<>();
                    list.add(strategyAction);
                    list.add(new Separator());

                    list.addAll(super.getNavigationActions());

                    return list;
                }
            };
            DiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, leftFile, rightFile);
            diffRequestProcessor.setRequest(request);

            add(diffRequestProcessor.getComponent(), BorderLayout.CENTER);
        } catch (Exception ex) {
            LOG.debug("Couldn't create diff, reason: {}", ex.getMessage(), ex);
            MidPointUtils.publishExceptionNotification(
                    project, null, ObjectDeltaPanel.class, ObjectDeltaEditor.NOTIFICATION_KEY,
                    "Couldn't create diff, reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();

        diffRequestProcessor.updateRequest();
    }
}
