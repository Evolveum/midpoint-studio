package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tree table model for {@link OperationResultDialog}. Columns: Operation, Status, Message, Context.
 * The tree structure mirrors the {@link OperationResult} subresult hierarchy.
 */
public class OperationResultTableModel extends DefaultTreeTableModel<OperationResult> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public OperationResultTableModel(@NotNull OperationResult result) {
        super((List<ColumnInfo>) (List<?>) List.of(
                new DefaultColumnInfo<OperationResult, String>("Operation", String.class, null).preferredWidth(150),
                new DefaultColumnInfo<OperationResult, OperationResultStatus>("Status", OperationResultStatus.class, null).preferredWidth(50),
                new DefaultColumnInfo<OperationResult, String>("Message", String.class, null).preferredWidth(500).minWidth(200),
                new DefaultColumnInfo<OperationResult, String>("Context", String.class, null).preferredWidth(150)
        ));

        setRoot(buildNode(result));
    }

    private static DefaultMutableTreeNode buildNode(OperationResult result) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(result);
        result.getSubresults().forEach(sub -> node.add(buildNode(sub)));
        return node;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (!(node instanceof DefaultMutableTreeNode dmtn)) {
            return null;
        }
        if (!(dmtn.getUserObject() instanceof OperationResult result)) {
            return null;
        }
        return switch (column) {
            case 0 -> result.getOperation().replace("com.evolveum.midpoint", "..");
            case 1 -> result.getStatus();
            case 2 -> result.getMessage() != null ? result.getMessage() : "";
            case 3 -> formatContext(result.getContext());
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    private static String formatContext(Map<String, Collection<String>> ctx) {
        if (ctx == null || ctx.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Collection<String>> e : ctx.entrySet()) {
            sb.append(e.getKey()).append(":").append(StringUtils.join(e.getValue(), ',')).append('\n');
        }
        return sb.toString();
    }
}
