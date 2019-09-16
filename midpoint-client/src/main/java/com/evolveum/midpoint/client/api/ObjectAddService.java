package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface ObjectAddService<O extends ObjectType> {

    String post() throws AuthenticationException;

    String post(AddOptions opts) throws AuthenticationException;
}
