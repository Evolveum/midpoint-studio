package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface ObjectAddService<O extends ObjectType> {

    String execute() throws AuthenticationException;

    String execute(List<String> opts) throws AuthenticationException;
}
