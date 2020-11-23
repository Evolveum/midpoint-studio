package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensProjectionContextType;
import com.intellij.openapi.diagnostic.Logger;

import java.util.Objects;

public class LensContextNode extends PrismNode {

    private static final Logger LOG = Logger.getInstance(LensContextNode.class);

    private final LensContextType lensContext;

    private final String label;

    public LensContextNode(String label, LensContextType lensContext) {
        super(null);

        this.lensContext = lensContext;
        this.label = label;

        try {
            createChildren();
        } catch (SchemaException e) {
            LOG.debug("Couldn't build lens context node", e);

            label += " " + e.getMessage();
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue(int i) {
        return "";
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
        LensContextNode other = (LensContextNode) obj;
        return Objects.equals(getLabel(), other.getLabel());
//		if (objects == null) {
//			if (other.objects != null)
//				return false;
//		} else if (!objects.equals(other.objects))
//			return false;
//		return true;
    }

    private void createChildren() throws SchemaException {
        if (lensContext == null) {
            return;
        }

        LensElementContextNode.create(lensContext.getFocusContext(), this);

        for (LensProjectionContextType pctx : lensContext.getProjectionContext()) {
            LensElementContextNode.create(pctx, this);
        }
    }

}
