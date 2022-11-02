package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.traces.operations.ProjectorProjectionOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.trace.entry.ObjectDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import com.intellij.openapi.diagnostic.Logger;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 *
 */
public class ProjectorProjectionOverviewProvider implements OverviewProvider<ProjectorProjectionOpNode> {

    private static final Logger LOG = Logger.getInstance(ProjectorProjectionOverviewProvider.class);

    @Override
    public void provideOverview(ProjectorProjectionOpNode node, DefaultMutableTreeTableNode root,
                                ViewingState initialState) throws SchemaException {

        ProjectorComponentTraceType trace = node.getTrace();
        if (trace != null) {

            ShadowDiscriminatorType rsd = getRsd(node, trace);

            PrismValueNode.create("Discriminator", rsd != null ? rsd : null, root);

            LensContextType inputContext = trace.getInputLensContext();
            if (inputContext != null) {
                LensFocusContextType focusContext = inputContext.getFocusContext();
                if (focusContext != null) {
                    PrismValueNode.create("Focus old", focusContext.getObjectOld(), root);
                    PrismValueNode.create("Focus current", focusContext.getObjectCurrent(), root);
                    ObjectDeltaTypeNode.create("Focus primary delta: ", false, focusContext.getPrimaryDelta(), node.getFocusName(), root);
                    ObjectDeltaTypeNode.create("Focus secondary delta (before): ", false, focusContext.getSecondaryDelta(), node.getFocusName(), root);
                }

                LensProjectionContextType projCtx = findProjectionContext(inputContext, rsd);
                String projectionName = getProjectionName(projCtx);

                if (projCtx != null) {
                    PrismValueNode.create("Projection old", projCtx.getObjectOld(), root);
                    PrismValueNode.create("Projection current", projCtx.getObjectCurrent(), root)
                            .setBackgroundColor(Colors.INPUT_1_COLOR, true);

                    ObjectDeltaType syncDelta = projCtx.getSyncDelta();
                    Node<?> syncDeltaNode = ObjectDeltaTypeNode.create("Projection sync delta: ", false, syncDelta, projectionName, root);
                    if (syncDeltaNode != null) {
                        syncDeltaNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);
                    }
                    ObjectDeltaType primaryDelta = projCtx.getPrimaryDelta();
                    Node<?> primaryDeltaNode = ObjectDeltaTypeNode.create("Projection primary delta: ", false, primaryDelta, projectionName, root);
                    if (primaryDeltaNode != null) {
                        primaryDeltaNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);
                    }
                    ObjectDeltaType secondaryDelta = projCtx.getSecondaryDelta();
                    Node<?> secDeltaNode = ObjectDeltaTypeNode.create("Projection secondary delta (before): ", false, secondaryDelta, projectionName, root);
                    if (secDeltaNode != null) {
                        secDeltaNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
                    }
                }
            }

            LensContextType outputContext = trace.getOutputLensContext();
            if (outputContext != null) {
                LensFocusContextType focusContext = outputContext.getFocusContext();
                if (focusContext != null) {
                    ObjectDeltaType secondaryDelta = focusContext.getSecondaryDelta();
                    ObjectDeltaTypeNode.create("Secondary delta (after): ", false, secondaryDelta, node.getFocusName(), root);
                    PrismValueNode.create("Focus new", focusContext.getObjectNew(), root);
                }
                LensProjectionContextType projCtx = findProjectionContext(outputContext, rsd);
                String projectionName = getProjectionName(projCtx);

                if (projCtx != null) {
                    ObjectDeltaType secondaryDelta = projCtx.getSecondaryDelta();
                    Node<?> secDeltaNode = ObjectDeltaTypeNode.create("Projection secondary delta (after): ", false, secondaryDelta, projectionName, root);
                    if (secDeltaNode != null) {
                        secDeltaNode.setBackgroundColor(Colors.OUTPUT_2_COLOR, true);
                    }
                    PrismValueNode.create("Projection new", projCtx.getObjectNew(), root)
                            .setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                }
            }
        }
    }

    private String getProjectionName(LensProjectionContextType projCtx) {
        if (projCtx == null) {
            return null;
        }
        if (projCtx.getObjectNew() != null) {
            return PolyString.getOrig(projCtx.getObjectNew().getName());
        } else if (projCtx.getObjectCurrent() != null) {
            return PolyString.getOrig(projCtx.getObjectCurrent().getName());
        } else if (projCtx.getObjectOld() != null) {
            return PolyString.getOrig(projCtx.getObjectOld().getName());
        } else {
            return null;
        }
    }

    private LensProjectionContextType findProjectionContext(LensContextType context, ShadowDiscriminatorType rsd) {
        if (rsd == null) {
            if (context.getProjectionContext().size() == 1) {
                return context.getProjectionContext().get(0); // TODO ok?
            } else {
                return null;
            }
        }

        for (LensProjectionContextType projCtx : context.getProjectionContext()) {
            ShadowDiscriminatorType projRsd = projCtx.getResourceShadowDiscriminator();
            if (projRsd != null && projRsd.getResourceRef() != null && projRsd.getResourceRef().getOid() != null &&
                    projRsd.getResourceRef().equals(rsd.getResourceRef()) &&
                    defaultIfNull(projRsd.getKind(), ShadowKindType.ACCOUNT) == defaultIfNull(rsd.getKind(), ShadowKindType.ACCOUNT) &&
                    defaultIfNull(projRsd.getIntent(), "default").equals(defaultIfNull(rsd.getIntent(), "default"))) {
                return projCtx;
            }
        }
        return null;
    }

    private ShadowDiscriminatorType getRsd(ProjectorProjectionOpNode node, ProjectorComponentTraceType trace) {
        if (trace.getResourceShadowDiscriminator() != null) {
            return trace.getResourceShadowDiscriminator();
        } else {
            return getRsdGuessed(node);
        }
    }

    private ShadowDiscriminatorType getRsdGuessed(ProjectorProjectionOpNode node) {
        List<String> qualifiers = node.getResult().getQualifier();
        if (qualifiers.size() != 1) {
            LOG.info("Wrong # of qualifiers: " + qualifiers);
            return null;
        }
        // e.g. "INITIAL.e0p0.10000000-0000-0000-0000-000000000004.ACCOUNT.default"
        String[] parts = qualifiers.get(0).split("\\.");
        if (parts.length != 5) {
            LOG.info("Wrong # of parts: " + qualifiers);
        }
        try {
            ShadowDiscriminatorType sd = new ShadowDiscriminatorType();
            sd.setResourceRef(new ObjectReferenceType()
                    .oid(parts[2])
                    .type(ResourceType.COMPLEX_TYPE));
            sd.kind(ShadowKindType.fromValue(parts[3]));
            sd.setIntent(parts[4]);
            sd.setTag(null);

            // todo set gone = false, probably via tombstone
            sd.setTombstone(false);

            return sd;
        } catch (Throwable t) {
            LOG.info("Exception: " + t);
            return null;
        }
    }
}
