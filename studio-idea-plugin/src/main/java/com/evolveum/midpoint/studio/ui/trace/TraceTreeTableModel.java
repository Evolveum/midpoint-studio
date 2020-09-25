package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceTreeTableModel extends DefaultTreeTableModel {

    @NotNull private final List<TreeTableColumnDefinition<OpNode, ?>> columnDefinitions;
    @Nullable private final OpNode rootOpNode;
    @NotNull private final RootTraceTreeTableNode invisibleRootTreeNode;

    @NotNull private final Map<OpNode, RegularTraceTreeTableNode> convertedNodeMap = new HashMap<>();

    public TraceTreeTableModel(@NotNull List<TreeTableColumnDefinition<OpNode, ?>> columnDefinitions,
            @Nullable OpNode rootOpNode) {

        this.columnDefinitions = columnDefinitions;
        this.rootOpNode = rootOpNode;
        this.invisibleRootTreeNode = new RootTraceTreeTableNode(rootOpNode);

        if (rootOpNode != null) {
            createNodeMap(rootOpNode);
            System.out.println("convertedNodeMap: " + convertedNodeMap.size() + " entries");
        }

        updateParentChildLinks();

        setColumnIdentifiers(getColumnNames());
        setRoot(invisibleRootTreeNode);
    }

    private void createNodeMap(OpNode opNode) {
        convertedNodeMap.put(opNode, new RegularTraceTreeTableNode(opNode));
        opNode.getChildren().forEach(this::createNodeMap);
    }

    public void updateParentChildLinks() {
        if (rootOpNode == null) {
            return;
        }

        invisibleRootTreeNode.clearParentChildLinks();
        convertedNodeMap.values().forEach(AbstractTraceTreeTableNode::clearParentChildLinks);

        if (rootOpNode.isVisible()) {
            RegularTraceTreeTableNode realRootTreeNode = convertedNodeMap.get(rootOpNode);
            updateParentChildLinks(realRootTreeNode);
            invisibleRootTreeNode.addChild(realRootTreeNode);
        } else {
            updateParentChildLinks(invisibleRootTreeNode);
        }
    }

    // TODO do better
    public void fireChange() {
        setRoot(invisibleRootTreeNode);
    }

    private void updateParentChildLinks(AbstractTraceTreeTableNode node) {
        for (OpNode visibleChild : node.getUserObject().getVisibleChildren()) {
            RegularTraceTreeTableNode visibleTreeTableChild = convertedNodeMap.get(visibleChild);
            node.addChild(visibleTreeTableChild);
            updateParentChildLinks(visibleTreeTableChild);
        }
    }

    @NotNull
    private List<String> getColumnNames() {
        return columnDefinitions.stream()
                .map(TreeTableColumnDefinition::getHeader)
                .collect(Collectors.toList());
    }

    @Override
    public Object getValueAt(Object node, int column) {
        AbstractTraceTreeTableNode d = (AbstractTraceTreeTableNode) node;
        if (d == null || d.getUserObject() == null) {
            return null;
        } else {
            Object value = columnDefinitions.get(column).getValue().apply(d.getUserObject());
            if (value == null) {
                return "";
            }
            if (isDisabled(d) && !DisplayUtil.isHtml(value)) {
                return DisplayUtil.makeDisabled(value);
            } else {
                return value;
            }
        }
    }

    private boolean isDisabled(AbstractTraceTreeTableNode d) {
        OpNode opNode = d.getUserObject();
        return opNode != null && (opNode.isDisabled() || isNotApplicable(opNode.getResult()));
    }

    private boolean isNotApplicable(OperationResultType result) {
        return result != null && result.getStatus() == OperationResultStatusType.NOT_APPLICABLE;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void firePathChanged(TreePath path) {
        modelSupport.fireTreeStructureChanged(path);
    }
}
