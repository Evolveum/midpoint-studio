package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.studio.ui.trace.presentation.AbstractOpNodePresentation;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

/**
 * Display overview of given OpNode.
 *
 * Created by Viliam Repan (lazyman).
 */
public class OpNodeOverviewPanel extends OpNodeTreeViewPanel {

    private static final Logger LOG = Logger.getInstance(OpNodeOverviewPanel.class);

    public OpNodeOverviewPanel(@NotNull Project project) {
        super(project);
    }

    @Override
    void updateModel(OpNode node) {

        super.updateModel(node);

        ViewingState initialState = new ViewingState();

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        if (node != null) {
            root.add(new TextNode("Operation", node.getLabel()));
            //root.add(new TextNode("Duration", node.getMillisecondsFormatted() + " ms"));

            if (node.getType() != null) {
                try {
                    //noinspection unchecked
                    ((AbstractOpNodePresentation<OpNode>) node.getPresentation())
                            .getOverviewProvider()
                            .provideOverview(node, root, initialState);
                } catch (SchemaException e) {
                    TextNode.create("Couldn't provide overview", e.getMessage(), root);
                }
            }
        }
        updateTreeModel(root);
        setViewingState(initialState);
    }
}
