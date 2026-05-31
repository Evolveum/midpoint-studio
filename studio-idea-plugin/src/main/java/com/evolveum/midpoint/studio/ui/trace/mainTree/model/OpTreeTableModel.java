package com.evolveum.midpoint.studio.ui.trace.mainTree.model;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.presentation.AbstractOpNodePresentation;
import com.evolveum.midpoint.studio.ui.treetable.CellStyle;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree table model for the main OpNode tree in the trace viewer.
 */
public class OpTreeTableModel extends DefaultTreeTableModel<OpNode> {

    private static final Logger LOG = Logger.getInstance(OpTreeTableModel.class);

    @NotNull
    private final List<DefaultColumnInfo<OpNode, ?>> columnInfos;

    @Nullable
    private final OpNode rootOpNode;

    @NotNull
    private final RootOpTreeTableNode invisibleRootTreeNode;

    @NotNull
    private final Map<OpNode, RegularOpTreeTableNode> convertedNodeMap = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public OpTreeTableModel(
            @NotNull List<DefaultColumnInfo<OpNode, ?>> columnInfos,
            @Nullable OpNode rootOpNode) {

        super((List<ColumnInfo>) (List<?>) columnInfos);

        this.columnInfos = columnInfos;
        this.rootOpNode = rootOpNode;
        this.invisibleRootTreeNode = new RootOpTreeTableNode(rootOpNode);

        if (rootOpNode != null) {
            createNodeMap(rootOpNode);
            LOG.info("convertedNodeMap: " + convertedNodeMap.size() + " entries");
        }

        updateParentChildLinks();
        setRoot(invisibleRootTreeNode);

        // Row styler: disabled rows → grey foreground; presentation background → row background
//        setRowStyler(node -> {
//            if (node == null) {
//                return null;
//            }
//            Color fg = isDisabledNode(node) ? UIUtil.getLabelDisabledForeground() : null;
//            Color bg = getNodeBackground(node);
//            if (fg == null && bg == null) {
//                return null;
//            }
//            return CellStyle.of(fg, bg, null);
//        });
    }

    private void createNodeMap(OpNode opNode) {
        convertedNodeMap.put(opNode, new RegularOpTreeTableNode(opNode));
        opNode.getChildren().forEach(this::createNodeMap);
    }

    public void updateParentChildLinks() {
        if (rootOpNode == null) {
            return;
        }

        invisibleRootTreeNode.clearParentChildLinks();
        convertedNodeMap.values().forEach(AbstractOpTreeTableNode::clearParentChildLinks);

        if (rootOpNode.isVisible()) {
            RegularOpTreeTableNode realRootTreeNode = convertedNodeMap.get(rootOpNode);
            updateParentChildLinks(realRootTreeNode);
            invisibleRootTreeNode.addChild(realRootTreeNode);
        } else {
            updateParentChildLinks(invisibleRootTreeNode);
        }
    }

    public void fireChange() {
        setRoot(invisibleRootTreeNode);
    }

    public void firePathChanged(TreePath path) {
        fireTreeStructureChanged(this, path.getPath(), null, null);
    }

    private void updateParentChildLinks(AbstractOpTreeTableNode node) {
        for (OpNode visibleChild : node.getUserObject().getVisibleChildren()) {
            RegularOpTreeTableNode visibleTreeTableChild = convertedNodeMap.get(visibleChild);
            node.addChild(visibleTreeTableChild);
            updateParentChildLinks(visibleTreeTableChild);
        }
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (!(node instanceof AbstractOpTreeTableNode treeNode)) {
            return null;
        }
        OpNode opNode = treeNode.getUserObject();
        if (opNode == null) {
            return null;
        }
        // Call applyValueFunction to bypass the DefaultMutableTreeTableNode parameter in valueOf()
        return columnInfos.get(column).applyValueFunction(opNode);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    private boolean isDisabledNode(OpNode node) {
        return node.isDisabled() || isNotApplicable(node.getResult());
    }

    private boolean isNotApplicable(OperationResultType result) {
        return result != null && result.getStatus() == OperationResultStatusType.NOT_APPLICABLE;
    }

    @Nullable
    private Color getNodeBackground(OpNode node) {
        AbstractOpNodePresentation<?> presentation = (AbstractOpNodePresentation<?>) node.getPresentation();
        return presentation != null ? presentation.getBackgroundColor() : null;
    }
}
