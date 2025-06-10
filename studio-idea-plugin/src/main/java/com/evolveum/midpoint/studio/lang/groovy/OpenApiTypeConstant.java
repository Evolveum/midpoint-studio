package com.evolveum.midpoint.studio.lang.groovy;

import javax.annotation.Nullable;
import java.util.List;

public record OpenApiTypeConstant(
        String openApiFormat,
        String primaryWireType,
        @Nullable
        String availableWireTypes,
        @Nullable
        List<String> connidClass)
{
        public static final String OPEN_API_TYPE_MAPPING_ENUM = "com.evolveum.polygon.scim.rest.OpenApiTypeMapping";
}
