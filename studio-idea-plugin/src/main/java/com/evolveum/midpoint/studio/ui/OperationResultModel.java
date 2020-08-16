package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultModel extends TreeTableColumnModel<OperationResult, Object> {

    public OperationResultModel(@NotNull OperationResult result, @NotNull List<TreeTableColumnDefinition<OperationResult, Object>> columns) {
        super(null, columns);

        setRoot(initNode(result));
    }

    private MutableTreeTableNode initNode(OperationResult result) {
        DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(result);
        result.getSubresults().forEach(s -> node.add(initNode(s)));

        return node;
    }
}
