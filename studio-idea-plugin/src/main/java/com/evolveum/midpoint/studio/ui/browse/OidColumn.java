package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class OidColumn extends DefaultColumnInfo {

    public OidColumn() {
        super("Oid", o -> {
            if (o instanceof ObjectType object) {
                return object.getOid();
            }

            return null;
        });

        setMinWidth(50);
        setMaxWidth(500);
        setPreferredWidth(320);
    }
}
