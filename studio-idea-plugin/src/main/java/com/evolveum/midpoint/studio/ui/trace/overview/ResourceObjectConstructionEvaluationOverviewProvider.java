package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.FormattingUtil;
import com.evolveum.midpoint.schema.traces.operations.ResourceObjectConstructionEvaluationOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentPathType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectConstructionEvaluationTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowDiscriminatorType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class ResourceObjectConstructionEvaluationOverviewProvider implements OverviewProvider<ResourceObjectConstructionEvaluationOpNode> {

    @Override
    public void provideOverview(ResourceObjectConstructionEvaluationOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        ResourceObjectConstructionEvaluationTraceType trace = node.getTrace();
        if (trace != null) {
            PrismValueNode.create("Construction", trace.getConstruction(), root)
                    .setBackgroundColor(Colors.CONFIGURATION_1_COLOR, true);
            ShadowDiscriminatorType rsd = trace.getResourceShadowDiscriminator();
            if (rsd != null) {
                TextNode.create("Resource", FormattingUtil.getResourceName(rsd), root);
                TextNode.create("Kind", rsd.getKind(), root);
                TextNode.create("Intent", rsd.getIntent(), root);
                TextNode.create("Tag", rsd.getTag(), root);
            }
            AssignmentPathType assignmentPath = trace.getAssignmentPath();
            if (assignmentPath != null) {
                PrismValueNode.create("Assignment path", assignmentPath, root);
            }
        } else {
            TextNode.create("Info", node.getInfo(), root);
        }
    }
}
