package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MidPointClient {

    void init() throws Exception;

    <T extends ObjectType> List<T> list(Class<T> type);

    <T extends ObjectType> T get(Class<T> type, String oid);
}
