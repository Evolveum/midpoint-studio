package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;
import com.evolveum.midpoint.studio.ui.delta.ObjectDeltaEditor;
import com.evolveum.midpoint.studio.ui.delta.ObjectDeltaPanel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleDiffPanel<O extends ObjectType> extends BorderLayoutPanel implements Disposable {

    private final Project project;

    private CustomComboBoxAction<DiffStrategy> strategyAction;

    private PrismObject<O> left;

    private PrismObject<O> right;

    private LightVirtualFile leftFile;

    private DiffSourceType leftSourceType;

    private LightVirtualFile rightFile;

    private DiffSourceType rightSourceType;

    //    private CacheDiffRequestChainProcessor processor;
    private SimpleDiffRequestProcessor processor;

    public SimpleDiffPanel(@NotNull Project project, PrismObject<O> left, DiffSourceType leftSourceType, PrismObject<O> right, DiffSourceType rightSourceType) {
        this.project = project;

        this.left = left;
        this.leftSourceType = leftSourceType;
        this.right = right;
        this.rightSourceType = rightSourceType;

        initLayout();

        refreshView();
    }

    public JComponent getPreferredFocusedComponent() {
        return processor.getComponent();
    }

    public PrismObject<O> getRight() {
        return right;
    }

    public void setRight(PrismObject<O> right) {
        this.right = right;
    }

    public PrismObject<O> getLeft() {
        return left;
    }

    public void setLeft(PrismObject<O> left) {
        this.left = left;
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void initLayout() {
        strategyAction = new DiffStrategyComboAction(DiffStrategy.DEFAULT) {

            @Override
            public void setSelected(DiffStrategy selected) {
                super.setSelected(selected);

                refreshView();
            }
        };

        leftFile = new LightVirtualFile(getName(left, leftSourceType), "");
        rightFile = new LightVirtualFile(getName(right, rightSourceType), "");

//        processor.addListener(new DiffEditorViewerListener() {
//
//            @Override
//            public void onActiveFileChanged() {
//                System.out.println();
//            }
//        }, this);
        processor = new SimpleDiffRequestProcessor(project) {

//        processor = new CacheDiffRequestChainProcessor(project, new SimpleDiffRequestChain(request)) {

            @Override
            protected @org.jetbrains.annotations.NotNull List<AnAction> getNavigationActions() {
                List<AnAction> list = new ArrayList<>();
                list.add(strategyAction);
                list.add(new Separator());

                list.addAll(super.getNavigationActions());

                return list;
            }
        };
        add(processor.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        processor.updateRequest();
    }

    private void refreshView() {
        PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;

        DiffStrategy strategy = strategyAction.getSelected();
        ParameterizedEquivalenceStrategy pes = strategy != null ?
                strategy.getStrategy() : ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION;

        try {
            PrismObject o1 = left;
            PrismObject o2 = right.clone();

            ObjectDelta delta = left.diff(right, pes);
            delta.applyTo(o2);

            leftFile.setContent(this, ClientUtils.serialize(prismContext, o1), true);
            rightFile.setContent(this, ClientUtils.serialize(prismContext, o2), true);

            DiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, leftFile, rightFile);
            processor.setRequest(request);
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(project, null, ObjectDeltaPanel.class, ObjectDeltaEditor.NOTIFICATION_KEY,
                    "Couldn't create diff, reason: " + ex.getMessage(), ex);
        }
    }

    private String getName(PrismObject<O> object, DiffSourceType type) {
        if (object == null) {
            return "undefined";
        }

        PolyString name = object.getName();
        String fullName = name != null ? name.getOrig() : "undefined";

        return fullName + " (" + type + ")";
    }
}
