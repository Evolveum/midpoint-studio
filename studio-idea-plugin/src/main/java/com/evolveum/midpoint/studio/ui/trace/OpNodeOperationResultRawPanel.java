package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OpNodeOperationResultRawPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(OpNodeOperationResultRawPanel.class);

    private JBTextArea text;

    public OpNodeOperationResultRawPanel(@NotNull Project project) {
        initLayout();

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void initLayout() {
        text = new JBTextArea();

        add(new JBScrollPane(text), BorderLayout.CENTER);
    }

    private void nodeChange(OpNode node) {
        if (node == null) {
            this.text.setText(null);
        } else {
            this.text.setText(dump(node.getResult()));
            this.text.setCaretPosition(0);
        }
    }

    private String dump(OperationResultType operationResult) {
        try {
            PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
            return ctx.xmlSerializer().serializeRealValue(operationResult, new QName("operationResult"));
        } catch (Exception ex) {
            LOG.error("Couldn't serialize value", ex);
            return ex.toString();
        }
    }
}
