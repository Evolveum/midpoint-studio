package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface ObjectAddService<O extends ObjectType> {

    String add() throws AuthenticationException;

    String add(AddOptions opts) throws AuthenticationException;
}
