package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import javax.xml.namespace.QName;

/**
 *
 */
public class FormattingContext {

    private final OpNode currentOpNode;
    private final NameResolver resolver;

    public FormattingContext(OpNode currentOpNode) {
        this.currentOpNode = currentOpNode;
        this.resolver = createNameResolver();
    }

    private NameResolver createNameResolver() {
        if (currentOpNode == null || currentOpNode.getTraceInfo() == null || currentOpNode.getTraceInfo().getTracingOutput() == null) {
            return null;
        }
        TracingOutputType tracingOutput = currentOpNode.getTraceInfo().getTracingOutput();
        if (tracingOutput.getDictionary() == null) {
            return null;
        } else {
            return new NameResolver(tracingOutput.getDictionary());
        }
    }

    public void addObjectNames(Object obj) {

        if (resolver == null) {
            return;
        }

        if (obj instanceof Visitable) {
            //noinspection unchecked
            ((Visitable<?>) obj).accept(resolver.createVisitor());
        } else if (obj instanceof JaxbVisitable) {
            ((JaxbVisitable) obj).accept(resolver.createVisitor());
        } else if (obj instanceof Containerable) {
            ((Containerable) obj).asPrismContainerValue().accept(resolver.createVisitor());
        }
    }

    // FIXME ugly hack
    private static class NameResolver extends StudioNameResolver {

        public NameResolver(TraceDictionaryType dictionary) {
            super(dictionary, null); // todo file
        }

        public void resolve(PrismReferenceValue reference) {
            if (reference != null && reference.getOid() != null && reference.getTargetName() == null) {
                PolyStringType name = getName(reference.getOid());
                if (name != null) {
                    reference.setTargetName(name.toPolyString());
                }
            }
        }

        private void resolveOidInMapXNode(MapXNode map) {
            XNode oidNode = map.get(new QName("oid"));
            if (oidNode instanceof PrimitiveXNode) {
                String oid = ((PrimitiveXNode<?>) oidNode).getStringValue();
                if (oid != null) {
                    PolyStringType name = getName(oid);
                    if (name != null) {
                        ((MapXNodeImpl) map).setComment(name.getOrig());
                    }
                }
            }
        }

        public ResolvingVisitor createVisitor() {
            return new ResolvingVisitor();
        }

        private class ResolvingVisitor implements Visitor, JaxbVisitor {

            @Override
            public void visit(JaxbVisitable visitable) {
                resolveVisitable(visitable);
                if (visitable instanceof RawType && !((RawType) visitable).isParsed()) {
                    XNode xnode = ((RawType) visitable).getXnode();
                    if (xnode != null) {
                        //noinspection unchecked
                        xnode.accept(this);
                    }
                } else {
                    JaxbVisitable.visitPrismStructure(visitable, this);
                }
            }

            @Override
            public void visit(Visitable visitable) {
                resolveVisitable(visitable);
            }

            private void resolveVisitable(Object visitable) {
                if (visitable instanceof PrismReferenceValue) {
                    resolve((PrismReferenceValue) visitable);
                } else if (visitable instanceof ObjectReferenceType) {
                    resolve(((ObjectReferenceType) visitable).asReferenceValue());
                } else if (visitable instanceof MapXNode) {
                    resolveOidInMapXNode((MapXNode) visitable);
                }
            }
        }
    }
}
