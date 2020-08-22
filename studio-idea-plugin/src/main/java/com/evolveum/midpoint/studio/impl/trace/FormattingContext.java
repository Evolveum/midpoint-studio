package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.apache.commons.collections4.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private static class NameResolver {

        Map<String, Set<String>> objectMap = new HashMap<>();

        public NameResolver(TraceDictionaryType dictionary) {
            for (TraceDictionaryEntryType entry : dictionary.getEntry()) {
                PrismObject<?> object = entry != null && entry.getObject() != null ? entry.getObject().getObject() : null;
                if (object != null) {
                    Set<String> names = objectMap.computeIfAbsent(object.getOid(), s -> new HashSet<>());
                    CollectionUtils.addIgnoreNull(names, PolyString.getOrig(object.getName()));
                }
            }
        }

        public void resolve(PrismReferenceValue reference) {
            if (reference != null && reference.getOid() != null && reference.getTargetName() == null) {
                String name = resolveOid(reference.getOid());
                if (name != null) {
                    reference.setTargetName(PolyString.fromOrig(name));
                }
            }
        }

        private String resolveOid(String oid) {
            Set<String> names = objectMap.get(oid);
            if (names == null || names.isEmpty()) {
                return null;
            } else {
                return String.join(", ", names);
            }
        }

        private void resolveOidInMapXNode(MapXNode map) {
            XNode oidNode = map.get(new QName("oid"));
            if (oidNode instanceof PrimitiveXNode) {
                String oid = ((PrimitiveXNode<?>) oidNode).getStringValue();
                if (oid != null) {
                    String name = resolveOid(oid);
                    if (name != null) {
                        ((MapXNodeImpl) map).setComment(name);
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
