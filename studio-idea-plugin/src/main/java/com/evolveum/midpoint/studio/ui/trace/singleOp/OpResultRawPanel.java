package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OpResultRawPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(OpResultRawPanel.class);

    private SimpleCheckboxAction showChildren;
    private SimpleCheckboxAction loadAutomatically;

    private JBTextArea text;
    private OpNode currentNode;
    private boolean loaded;

    public OpResultRawPanel(@NotNull Project project) {
        initLayout();

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node, false);
            }
        });
    }

    private void initLayout() {
        text = new JBTextArea();

        add(new JBScrollPane(text), BorderLayout.CENTER);

        DefaultActionGroup group = new DefaultActionGroup();
        showChildren = new SimpleCheckboxAction("Show children") {
            @Override
            public void onStateChange() {
                nodeChange(currentNode, loaded);
            }
        };
        group.add(showChildren);
        loadAutomatically = new SimpleCheckboxAction("Load automatically");
        group.add(loadAutomatically);

        AnAction load = new AnAction("Load", "Load full log", AllIcons.Actions.Commit) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                nodeChange(currentNode, true);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!loaded);
            }
        };
        group.add(load);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewVariablesToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void nodeChange(OpNode node, boolean forceLoad) {
        this.currentNode = node;
        if (node == null) {
            this.text.setText(null);
        } else {
            if (loadAutomatically.isSelected() || forceLoad) {
                this.text.setText(dump(node.getResult()));
                this.text.setCaretPosition(0);
                loaded = true;
            } else {
                loaded = false;
            }
        }
    }

    private String dump(OperationResultType operationResult) {
        try {
            long start = System.currentTimeMillis();
            OperationResultType resultToShow;
            if (showChildren.isSelected()) {
                resultToShow = operationResult;
            } else {
                resultToShow = copyWithoutChildren(operationResult);
            }
            PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
            String xml = ctx.xmlSerializer().serializeRealValue(resultToShow, new QName("operationResult"));
            System.out.println("Raw OpNode serialized in " + (System.currentTimeMillis() - start) + " ms; children = " + showChildren.isSelected());
            return xml;
        } catch (Exception ex) {
            LOG.error("Couldn't serialize value", ex);
            return ex.toString();
        }
    }

    private OperationResultType copyWithoutChildren(OperationResultType original) {
        OperationResultType shallowClone = new OperationResultType();
        try {
            PropertyUtils.copyProperties(shallowClone, original);
        } catch (Throwable t) {
            System.out.println("Couldn't clone operation result, returning original");
            t.printStackTrace();
            return original;
        }
        shallowClone.getPartialResults().clear();
        return shallowClone;
    }
}
