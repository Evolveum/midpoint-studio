package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TraceParser {
	
	private TracingOutputType tracingOutput;
	private OperationResultType rootResult;
	private IdentityHashMap<OperationResultType, OpResultInfo> infoMap = new IdentityHashMap<>();
	
	public List<OpNode> parse(File traceFile) throws SchemaException, IOException {
		boolean isZip = traceFile.getName().toLowerCase().endsWith(".zip");
		return parse(new FileInputStream(traceFile), isZip);
	}
	
	public List<OpNode> parse(InputStream stream, boolean isZip) throws SchemaException, IOException {
		tracingOutput = getObject(stream, isZip);
		if (tracingOutput != null) {
			rootResult = tracingOutput.getResult();
			expandDictionary(rootResult, new ExpandingVisitor(tracingOutput.getDictionary(), MidPointUtils.DEFAULT_PRISM_CONTEXT));	// todo switch prism context
		} else {
			rootResult = null;
		}
		if (rootResult != null) {
			List<OpNode> rv = new ArrayList<>();
			addNode(null, rv, rootResult, new TraceInfo(tracingOutput));
			return rv;
		} else {
			return new ArrayList<>();
		}
	}
	
	private static class ExpandingVisitor implements Visitor {

		private final TraceDictionaryType dictionary;
		private final PrismContext prismContext;

		private ExpandingVisitor(TraceDictionaryType dictionary, PrismContext prismContext) {
			this.dictionary = dictionary;
			this.prismContext = prismContext;
		}

//		@Override
//		public void visit(JaxbVisitable visitable) {
//			JaxbVisitable.visitPrismStructure(visitable, this);
//		}

		@Override
		public void visit(Visitable visitable) {
//			if (visitable instanceof PrismPropertyValue) {
//				PrismPropertyValue<?> pval = ((PrismPropertyValue) visitable);
//				Object realValue = pval.getRealValue();
//				if (realValue instanceof JaxbVisitable) {
//					((JaxbVisitable) realValue).accept(this);
//				}
//			} else
			if (visitable instanceof PrismReferenceValue) {
				PrismReferenceValue refVal = (PrismReferenceValue) visitable;
				if (refVal.getObject() == null && refVal.getOid() != null && refVal.getOid().startsWith(SchemaConstants.TRACE_DICTIONARY_PREFIX)) {
					String id = refVal.getOid().substring(SchemaConstants.TRACE_DICTIONARY_PREFIX.length());
					TraceDictionaryEntryType entry = findEntry(id);
					if (entry == null) {
						System.err.println("No dictionary entry #" + id);
					} else if (entry.getObject() == null) {
						System.err.println("No object in dictionary entry #" + id);
					} else if (entry.getObject().asReferenceValue().getObject() == null) {
						System.err.println("No embedded object in dictionary entry #" + id);
					} else {
						PrismObject object = entry.getObject().asReferenceValue().getObject();
						refVal.setObject(object);
						refVal.setOid(object.getOid());
						//System.out.println("Expanded entry #" + id + " into " + object);
					}
				}
			}
		}

		private TraceDictionaryEntryType findEntry(String id) {
			for (TraceDictionaryEntryType entry : dictionary.getEntry()) {
				String qualifiedId = entry.getOriginDictionaryId() + ":" + entry.getIdentifier();
				if (qualifiedId.equals(id)) {
					return entry;
				}
			}
			return null;
		}
	}

	private void expandDictionary(OperationResultType resultBean, ExpandingVisitor expandingVisitor) {
		resultBean.getTrace().forEach(trace -> trace.asPrismContainerValue().accept(expandingVisitor));
		resultBean.getPartialResults().forEach(partialResult -> expandDictionary(partialResult, expandingVisitor));
	}

	public TracingOutputType getObject(InputStream stream, boolean isZip) throws IOException, SchemaException {
		Object object;
		PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;	// todo switch prism context
		if (isZip) {
			try (ZipInputStream zis = new ZipInputStream(stream)) {
				ZipEntry zipEntry = zis.getNextEntry();
				if (zipEntry != null) {
					object = prismContext.parserFor(zis).xml().parseRealValue();
				} else {
					System.err.println("No zip entry in input file");		// TODO error handling
					object = null;
				}
			}
		} else {
			ParsingContext parsingContext = prismContext.createParsingContextForCompatibilityMode();
			object = prismContext.parserFor(stream).xml().context(parsingContext).parseRealValue();
		}
		stream.close();
		if (object instanceof TracingOutputType) {
			return (TracingOutputType) object;
		} else if (object instanceof OperationResultType) {
			TracingOutputType rv = new TracingOutputType(prismContext);
			rv.setResult((OperationResultType) object);
			return rv;
		} else {
			System.err.println("Wrong object type in input file: " + object);
			return null;
		}
	}

	private void addNode(OpNode parent, List<OpNode> rv, OperationResultType result, TraceInfo traceInfo) {
		OpResultInfo info = OpResultInfo.create(result, infoMap);
		OpNode newNode = new OpNode(result, info, parent, traceInfo);
		rv.add(newNode);
		for (OperationResultType child : result.getPartialResults()) {
			addNode(newNode, newNode.getChildren(), child, traceInfo);
		}
	}

	public long getStartTimestamp() {
		return rootResult != null ? XmlTypeConverter.toMillis(rootResult.getStart()) : 0;
	}
}
