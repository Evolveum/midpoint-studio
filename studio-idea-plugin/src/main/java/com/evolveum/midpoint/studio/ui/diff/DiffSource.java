package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DiffSource<O extends ObjectType>(
        @Nullable String name,
        @NotNull DiffSourceType type,
        @NotNull PrismObject<O> object) {

    public String getName() {
        if (StringUtils.isNotEmpty(name)) {
            return name;
        }

        return MidPointUtils.getName(object);
    }
}
