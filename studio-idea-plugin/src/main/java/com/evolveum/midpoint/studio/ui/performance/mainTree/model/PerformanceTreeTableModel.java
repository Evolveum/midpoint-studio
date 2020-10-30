package com.evolveum.midpoint.studio.ui.performance.mainTree.model;

import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.intellij.openapi.diagnostic.Logger;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class PerformanceTreeTableModel extends DefaultTreeTableModel {

    private static final Logger LOG = Logger.getInstance(PerformanceTreeTableModel.class);

    @NotNull
    private final List<TreeTableColumnDefinition<OperationPerformance, ?>> columnDefinitions;
    @Nullable
    private final OperationPerformance rootOpNode;

    @NotNull
    private final Map<OperationPerformance, PerformanceTreeTableNode> convertedNodeMap = new HashMap<>();

    public PerformanceTreeTableModel(@NotNull List<TreeTableColumnDefinition<OperationPerformance, ?>> columnDefinitions,
                            @Nullable OperationPerformance rootOpNode) {

        this.columnDefinitions = columnDefinitions;
        this.rootOpNode = rootOpNode;

        if (rootOpNode != null) {
            createNodeMap(rootOpNode);
            LOG.info("convertedNodeMap: " + convertedNodeMap.size() + " entries");
            updateParentChildLinks();

            setColumnIdentifiers(getColumnNames());
            setRoot(convertedNodeMap.get(rootOpNode));
        } else {
            System.out.println("Nothing here");
            // ???
        }
    }

    private void createNodeMap(OperationPerformance opNode) {
        convertedNodeMap.put(opNode, new PerformanceTreeTableNode(opNode));
        opNode.getChildren().forEach(this::createNodeMap);
    }

    public void updateParentChildLinks() {
        convertedNodeMap.values().forEach(PerformanceTreeTableNode::clearParentChildLinks);

        PerformanceTreeTableNode realRootTreeNode = convertedNodeMap.get(rootOpNode);
        updateParentChildLinks(realRootTreeNode);
    }

    // TODO do better
    public void fireChange() {
        //setRoot(invisibleRootTreeNode);
    }

    private void updateParentChildLinks(PerformanceTreeTableNode node) {
        for (OperationPerformance child : node.getUserObject().getChildren()) {
            PerformanceTreeTableNode childNode = convertedNodeMap.get(child);
            node.addChild(childNode);
            updateParentChildLinks(childNode);
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
        PerformanceTreeTableNode d = (PerformanceTreeTableNode) node;
        if (d == null || d.getUserObject() == null) {
            return null;
        } else {
            Object value = columnDefinitions.get(column).getValue().apply(d.getUserObject());
            if (value == null) {
                return "";
            }
            return value;
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void firePathChanged(TreePath path) {
        modelSupport.fireTreeStructureChanged(path);
    }
}
