package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface ResourceObjectService extends ObjectService<ResourceType> {

    OperationResult test();
}
