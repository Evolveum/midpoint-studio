package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.intellij.openapi.project.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class QueryGenerator extends Generator {

    @Override
    public String getLabel() {
        return "Query returning these objects";
    }

    @Override
    public String generate(Project project, List<ObjectType> objects, GeneratorOptions options) {
        if (objects.isEmpty()) {
            return null;
        }
        Document doc = DOMUtil.getDocument(SchemaConstantsGenerated.Q_QUERY);
        Element query = doc.getDocumentElement();
        Element filter = DOMUtil.createSubElement(query, QueryType.F_FILTER);
        Element inOid = DOMUtil.createSubElement(filter, SchemaConstantsGenerated.Q_IN_OID);
        for (ObjectType o : objects) {
            DOMUtil.createSubElement(inOid, SchemaConstantsGenerated.Q_VALUE).setTextContent(o.getOid());
        }
        return DOMUtil.serializeDOMToString(doc);
    }

    @Override
    public boolean supportsSymbolicReferences() {
        return false;        // not yet
    }
}
