package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.MidPointObject;

public record ObjectItem(
        FileItem item, String oid, String name, ObjectTypes type, MidPointObject local, MidPointObject remote) {
}
