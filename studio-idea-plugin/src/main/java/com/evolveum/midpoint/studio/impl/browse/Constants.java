package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String COMMON_NS = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
    public static final String TYPES_NS = "http://prism.evolveum.com/xml/ns/public/types-3";
    public static final String QUERY_NS = "http://prism.evolveum.com/xml/ns/public/query-3";
    public static final String SCEXT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/scripting/extension-3";
    public static final String MEXT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/extension-3";

    public static final String NS_PREFIX_MEXT = "mext";
    public static final String NS_PREFIX_Q = "q";

    public static final QName MEXT_OBJECT_TYPE_PREFIXED = prefixQName(SchemaConstants.MODEL_EXTENSION_OBJECT_TYPE, NS_PREFIX_MEXT);
    public static final QName MEXT_OBJECT_QUERY_PREFIXED = prefixQName(SchemaConstants.MODEL_EXTENSION_OBJECT_QUERY, NS_PREFIX_MEXT);
    public static final QName Q_FILTER_PREFIXED = prefixQName(SchemaConstantsGenerated.Q_FILTER, NS_PREFIX_Q);
    public static final QName Q_IN_OID_PREFIXED = prefixQName(SchemaConstantsGenerated.Q_IN_OID, NS_PREFIX_Q);
    public static final QName Q_VALUE_PREFIXED = prefixQName(SchemaConstantsGenerated.Q_VALUE, NS_PREFIX_Q);

    public static final String SCRIPT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/scripting-3";
    public static final QName Q_IN_OID_Q = new QName(QUERY_NS, "inOid", "q");
    public static final QName Q_EQUAL_Q = new QName(QUERY_NS, "equal", "q");
    public static final QName Q_PATH_Q = new QName(QUERY_NS, "path", "q");
    public static final QName Q_VALUE_Q = new QName(QUERY_NS, "value", "q");
    public static final QName Q_SUBSTRING = new QName(QUERY_NS, "substring");

    /**
     * now in client/midpointutils
     */
    @Deprecated
    public static final List<String> SCRIPTING_ACTIONS = Arrays.asList(
            "executeScript",
            "scriptingExpression",
            "sequence",
            "pipeline",
            "search",
            "filter",
            "select",
            "foreach",
            "action"
    );

    public static final String SYSCONFIG_OID = "00000000-0000-0000-0000-000000000001";

    private static QName prefixQName(QName qname, String prefix) {
        return new QName(qname.getNamespaceURI(), qname.getLocalPart(), prefix);
    }
}
