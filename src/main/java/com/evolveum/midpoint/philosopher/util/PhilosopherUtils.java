package com.evolveum.midpoint.philosopher.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PhilosopherUtils {

    public static JAXBContext createJAXBContext() throws JAXBException {
        return JAXBContext.newInstance("com.evolveum.midpoint.xml.ns._public.common.api_types_3:"
                + "com.evolveum.midpoint.xml.ns._public.common.audit_3:"
                + "com.evolveum.midpoint.xml.ns._public.common.common_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.resource_schema_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.scripting_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.scripting.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.report.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.resource.capabilities_3:"
                + "com.evolveum.midpoint.xml.ns._public.task.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.task.jdbc_ping.handler_3:"
                + "com.evolveum.midpoint.xml.ns._public.task.noop.handler_3:"
                + "com.evolveum.prism.xml.ns._public.annotation_3:"
                + "com.evolveum.prism.xml.ns._public.query_3:"
                + "com.evolveum.prism.xml.ns._public.types_3");
    }
}
