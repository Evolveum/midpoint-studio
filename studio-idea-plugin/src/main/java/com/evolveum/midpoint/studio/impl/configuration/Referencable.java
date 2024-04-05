package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;

import javax.xml.namespace.QName;

public interface Referencable {

    String getOid();

    QName getType();

    String getName();

    void setName(String name);

    default ObjectReferenceType toObjectReferenceType() {
        return new ObjectReferenceType()
                .oid(getOid())
                .type(getType());
    }
}
