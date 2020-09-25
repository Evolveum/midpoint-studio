package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.RepositoryCacheOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryAddTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryDeleteTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryModifyTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryOperationTraceType;

import java.awt.*;

/**
 *
 */
public class RepositoryCacheOperationPresentation extends AbstractOpNodePresentation<RepositoryCacheOpNode> {

    public RepositoryCacheOperationPresentation(RepositoryCacheOpNode node) {
        super(node);
    }

    @Override
    public Color getBackgroundColor() {
        if (node.isDisabled()) {
            return null;
        }

        RepositoryOperationTraceType trace = node.getTrace();
        if (trace instanceof RepositoryAddTraceType || trace instanceof RepositoryModifyTraceType || trace instanceof RepositoryDeleteTraceType) {
            return Colors.OUTPUT_2_COLOR_WEAK;
        } else {
            return null;
        }
    }
}
