package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OpDumpPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(OpDumpPanel.class);

    private JBTextArea text;

    public OpDumpPanel(@NotNull Project project) {
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
            return;
        }

        OperationResultType result = node.getResult();
        StringBuilder sb = new StringBuilder();
        for (TraceType trace : result.getTrace()) {
            sb.append(dump(trace));
            sb.append("\n------------------------------------------------------------------------\n");
        }
        sb.append("Operation:  ").append(result.getOperation()).append("\n");
        sb.append("Qualifier:  ").append(result.getQualifier()).append("\n");
        sb.append("Importance: ");
        if (result.getImportance() != null) {
            sb.append(result.getImportance());
        }
        sb.append("\n");
        sb.append("Status:     ").append(result.getStatus());
        if (result.getMessage() != null) {
            sb.append(": " + result.getMessage());
        }
        sb.append("\n");
        sb.append("Inv. ID:    ").append(result.getInvocationId()).append("\n");
        sb.append("\n");
        sb.append("Start:      ").append(result.getStart()).append("\n");
        sb.append("End:        ").append(result.getEnd()).append("\n");
        sb.append("Duration:   ").append(result.getMicroseconds() != null ? String.format(Locale.US, "%.1f ms", result.getMicroseconds() / 1000.0) : "?").append("\n");
        sb.append("\n");
        sb.append(dump(" - par: ", result.getParams()));
        sb.append(dump(" - ctx: ", result.getContext()));
        sb.append(dump(" - ret: ", result.getReturns()));
        sb.append("\n------------------------------------------------------------------------\n");

        this.text.setText(sb.toString());
    }


    private String dump(TraceType trace) {
        TraceType traceNoText;
        List<String> texts;
        if (trace instanceof MappingEvaluationTraceType) {
            texts = new ArrayList<>();
            texts.add(((MappingEvaluationTraceType) trace).getTextTrace());
            try {
                PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
                String xml = ctx.xmlSerializer().serializeRealValue(((MappingEvaluationTraceType) trace).getMapping(), new QName("mapping"));

                texts.add(xml);
            } catch (SchemaException ex) {
                LOG.error("Couldn't serialize value", ex);

                texts.add(ex.getMessage());
            }
            traceNoText = null;
        } else if (!trace.getText().isEmpty()) {
            traceNoText = trace.clone();
            traceNoText.asPrismContainerValue().removeProperty(TraceType.F_TEXT);
            texts = trace.getText();
        } else {
            traceNoText = trace;
            texts = Collections.emptyList();
        }

        String rv = "";
        if (traceNoText != null) {
            rv = trace.getClass().getSimpleName() + ":" + "\n" + traceNoText.asPrismContainerValue().debugDump().substring(8);        // hack to remove id=null
            rv += "\n------------------------------\n";
        }
        for (String text : texts) {
            rv += text;
            rv += "\n------------------------------\n";
        }
        return rv;
    }

    private String dump(String prefix, ParamsType params) {
        String rv = "";
        if (params != null) {
            for (EntryType e : params.getEntry()) {
                rv += prefix + e.getKey() + " = " + dump(e.getEntryValue()) + "\n";
            }
        }
        return rv;
    }

    public static String dump(JAXBElement<?> jaxb) {
        if (jaxb == null) {
            return "";
        }
        Object value = jaxb.getValue();
        if (value instanceof RawType) {
            return ((RawType) value).extractString();
        } else {
            return String.valueOf(value);
        }
    }
}
