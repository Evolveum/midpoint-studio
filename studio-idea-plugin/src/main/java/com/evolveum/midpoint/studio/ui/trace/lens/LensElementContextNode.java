package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LensElementContextNode extends PrismValueNode {

    private final LensElementContextType lensElementContext;
    private final List<ObjectType> objects;

    public LensElementContextNode(LensElementContextType lensElementContext, PrismNode parent) throws SchemaException {
        super(computeLabel(lensElementContext), computeValues(lensElementContext), parent);

        Validate.notNull(lensElementContext);

        this.lensElementContext = lensElementContext;
        this.objects = Arrays.asList(lensElementContext.getObjectOld(), lensElementContext.getObjectCurrent(), lensElementContext.getObjectNew());
    }

    private static String computeLabel(LensElementContextType lensElementContext) {
        if (lensElementContext instanceof LensFocusContextType) {
            LensFocusContextType focus = (LensFocusContextType) lensElementContext;
            return "Focus: " + focus.getObjectTypeClass();
        } else if (lensElementContext instanceof LensProjectionContextType) {
            LensProjectionContextType pctx = (LensProjectionContextType) lensElementContext;
            ShadowDiscriminatorType rsd = pctx.getResourceShadowDiscriminator();
            return "Projection: " + (rsd.getResourceRef() != null ? rsd.getResourceRef().getOid() : "(null)") + " / " + rsd.getKind() + " / " + rsd.getIntent();
        } else {
            return "??? " + lensElementContext;
        }
    }

    private static List<PrismContainerValue<?>> computeValues(LensElementContextType lensElementContext) {
        List<PrismContainerValue<?>> values = Arrays.asList(
                toPcv(lensElementContext.getObjectOld()), toPcv(lensElementContext.getObjectCurrent()), toPcv(lensElementContext.getObjectNew()));
        return values;
    }

    private static PrismContainerValue<?> toPcv(ObjectType object) {
        return object != null ? object.asPrismContainerValue() : null;
    }

    @Override
    public String getValue(int i) {
        return objects.get(i) != null ? "(present)" : "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
//		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LensElementContextNode other = (LensElementContextNode) obj;
        return Objects.equals(getLabel(), other.getLabel());
//		if (objects == null) {
//			if (other.objects != null)
//				return false;
//		} else if (!objects.equals(other.objects))
//			return false;
//		return true;
    }

    public static void create(LensElementContextType context, LensContextNode parent) throws SchemaException {
        if (context != null) {
            new LensElementContextNode(context, parent);
        }
    }
}
