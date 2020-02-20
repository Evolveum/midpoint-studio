package com.evolveum.midpoint.studio.impl.browse;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class Constants {

	public static final String MODEL_NS = "http://midpoint.evolveum.com/xml/ns/public/model/model-3";
	public static final String API_TYPES_NS = "http://midpoint.evolveum.com/xml/ns/public/common/api-types-3";
	public static final String COMMON_NS = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
	public static final String TYPES_NS = "http://prism.evolveum.com/xml/ns/public/types-3";
	public static final String QUERY_NS = "http://prism.evolveum.com/xml/ns/public/query-3";
	public static final String SCEXT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/scripting/extension-3";
	public static final String MEXT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/extension-3";

	public static final String SCRIPT_NS = "http://midpoint.evolveum.com/xml/ns/public/model/scripting-3";
	public static final QName Q_QUERY = new QName(QUERY_NS, "query");
	public static final QName Q_PAGING = new QName(QUERY_NS, "paging");
	public static final QName Q_ORDER_BY = new QName(QUERY_NS, "orderBy");
	public static final QName Q_OFFSET = new QName(QUERY_NS, "offset");
	public static final QName Q_MAX_SIZE = new QName(QUERY_NS, "maxSize");
	public static final QName Q_FILTER = new QName(QUERY_NS, "filter");
	public static final QName Q_FILTER_Q = new QName(QUERY_NS, "filter", "q");
	public static final QName Q_IN_OID = new QName(QUERY_NS, "inOid");
	public static final QName Q_IN_OID_Q = new QName(QUERY_NS, "inOid", "q");
	public static final QName Q_EQUAL_Q = new QName(QUERY_NS, "equal", "q");
	public static final QName Q_PATH_Q = new QName(QUERY_NS, "path", "q");
	public static final QName Q_VALUE = new QName(QUERY_NS, "value");
	public static final QName Q_VALUE_Q = new QName(QUERY_NS, "value", "q");
	public static final QName Q_OR = new QName(QUERY_NS, "or");
	public static final QName Q_AND = new QName(QUERY_NS, "and");
	public static final QName Q_SUBSTRING = new QName(QUERY_NS, "substring");
	public static final QName Q_MATCHING = new QName(QUERY_NS, "matching");
	public static final QName Q_PATH = new QName(QUERY_NS, "path");
	public static final QName Q_TYPE = new QName(QUERY_NS, "type");

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
}
