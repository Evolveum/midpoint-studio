package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.RepositoryCacheOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryAddTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryDeleteTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryModifyTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryOperationTraceType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.xml.namespace.QName;

/**
 *
 */
public class RepositoryCacheOperationOverviewProvider implements OverviewProvider<RepositoryCacheOpNode> {

    @Override
    public void provideOverview(RepositoryCacheOpNode node, DefaultMutableTreeTableNode root,
                                ViewingState initialState) throws SchemaException {

        RepositoryOperationTraceType trace = node.getTrace();
        if (trace instanceof RepositoryAddTraceType) {
            provideOverview((RepositoryAddTraceType) trace, root);
        } else if (trace instanceof RepositoryModifyTraceType) {
            provideOverview((RepositoryModifyTraceType) trace, root);
        } else if (trace instanceof RepositoryDeleteTraceType) {
            provideOverview((RepositoryDeleteTraceType) trace, root);
        } else {
            TextNode.create("Operation", trace.getClass() != null ? trace.getClass().getSimpleName() : "Unknown", root);
            TextNode.create("Cache use", trace.getCacheUse(), root);
            PrismValueNode.create("Global cache use", trace.getGlobalCacheUse(), root);
            PrismValueNode.create("Local cache use", trace.getLocalCacheUse(), root);
        }
    }

    private void provideOverview(RepositoryAddTraceType trace, DefaultMutableTreeTableNode root)
            throws SchemaException {
        if (trace.getObjectRef() != null) {
            TextNode.create("Type", getLocal(trace.getObjectRef().getType()), root);
        }
        TextNode.create("OID", trace.getOid(), root);
        if (trace.getObjectRef() != null) {
            ItemNode.create("Object", trace.getObjectRef().getObject(), root)
                    .setBackgroundColor(Colors.OUTPUT_2_COLOR_WEAK, true);
        }
        TextNode.create("Options", trace.getOptions(), root);
    }

    private void provideOverview(RepositoryModifyTraceType trace, DefaultMutableTreeTableNode root)
            throws SchemaException {
        TextNode.create("Type", getLocal(trace.getObjectType()), root);
        TextNode.create("OID", trace.getOid(), root);
        for (ItemDeltaType itemDelta : trace.getModification()) {
            ItemDeltaTypeNode.create(String.valueOf(itemDelta.getPath()), itemDelta, false, root)
                    .setBackgroundColor(Colors.OUTPUT_2_COLOR_WEAK, true);
        }
        TextNode.create("Options", trace.getOptions(), root);
    }

    private void provideOverview(RepositoryDeleteTraceType trace, DefaultMutableTreeTableNode root) {
        TextNode.create("Type", getLocal(trace.getObjectType()), root);
        TextNode.create("OID", trace.getOid(), root);
    }

    private String getLocal(QName type) {
        return type != null ? type.getLocalPart() : null;
    }
}
