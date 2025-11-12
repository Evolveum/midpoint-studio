/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component;

import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;

import javax.xml.namespace.QName;

public class ResourceDialogContext {

    SearchResultList<ObjectType> resources;

    String uploadedResourceOid;

    ResourceType resourceObjectType;

    QName objectClass;

    public SearchResultList<ObjectType> getResources() {
        return resources;
    }

    public void setResources(SearchResultList<ObjectType> resources) {
        this.resources = resources;
    }

    public String getUploadedResourceOid() {
        return uploadedResourceOid;
    }

    public void setUploadedResourceOid(String uploadedResourceOid) {
        this.uploadedResourceOid = uploadedResourceOid;
    }

    public ResourceType getResourceObjectType() {
        return resourceObjectType;
    }

    public void setResourceObjectType(ResourceType resourceObjectType) {
        this.resourceObjectType = resourceObjectType;
    }

    public QName getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(QName objectClass) {
        this.objectClass = objectClass;
    }
}
