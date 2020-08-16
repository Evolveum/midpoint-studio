package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.Date;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestServiceUtil implements ServiceUtil {

    private PrismContext prismContext;

    public RestServiceUtil(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    public PolyStringType createPoly(String s) {
        if (s == null) {
            return null;
        }

        return PolyString.toPolyStringType(new PolyString(s));
    }

    @Override
    public String getOrig(PolyStringType polyStringType) {
        return null;
    }

    @Override
    public ItemPathType createItemPathType(QName... qNames) {
        return null;
    }

    @Override
    public XMLGregorianCalendar asXMLGregorianCalendar(Date date) {
        return null;
    }

    @Override
    public String getClearValue(ProtectedStringType protectedStringType) {
        return null;
    }

    @Override
    public <T> T parse(Class<T> aClass, String s) throws SchemaException {
        return null;
    }
}
