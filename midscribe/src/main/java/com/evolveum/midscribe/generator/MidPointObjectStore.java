package com.evolveum.midscribe.generator;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MidPointObjectStore {

    void init() throws Exception;

    void destroy() throws Exception;

    PrismContext getPrismContext();

    <T extends ObjectType> List<T> list(Class<T> type);

    <T extends ObjectType> T get(Class<T> type, String oid);
}
