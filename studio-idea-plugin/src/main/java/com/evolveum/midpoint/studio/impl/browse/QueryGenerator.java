package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class QueryGenerator extends Generator {

	@Override
	public String getLabel() {
		return "Query returning these objects";
	}

	@Override
	public String generate(List<MidPointObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(Constants.Q_QUERY);
		Element query = doc.getDocumentElement();
		Element filter = DOMUtil.createSubElement(query, Constants.Q_FILTER);
		Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID);
		for (MidPointObject o : objects) {
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(o.getOid());
		}
		return DOMUtil.serializeDOMToString(doc);
	}

	@Override
	public boolean supportsSymbolicReferences() {
		return false;		// not yet
	}
}
