package com.evolveum.midpoint.studio.impl;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacet extends Facet<MidPointFacetConfiguration> {

    public MidPointFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name,
                         @NotNull MidPointFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }
}
