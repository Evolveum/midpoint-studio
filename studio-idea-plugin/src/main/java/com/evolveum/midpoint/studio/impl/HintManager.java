package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class HintManager {

    private Map<String, HintEntry> hints = new HashMap<>();

    public void cache(PrismObject object) {

    }

    public void drop(PrismObject object) {

    }

    public HintEntry getHintByOid(String oid) {
        return hints.get(oid);
    }

    public List<HintEntry> getHintsByName(String name) {
        List<HintEntry> result = new ArrayList<>();

        for (HintEntry entry : hints.values()) {
            String eName = entry.getName();
            if (StringUtils.isEmpty(eName)) {
                continue;
            }

            if (!eName.toLowerCase().contains(name.toLowerCase())) {
                continue;
            }

            result.add(entry);
        }

        return result;
    }
}
