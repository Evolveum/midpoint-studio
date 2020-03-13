package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetType extends FacetType<MidPointFacet, MidPointFacetConfiguration> {

    public static final FacetTypeId<MidPointFacet> FACET_TYPE_ID = new FacetTypeId<>("MidPointFacet");

    public static final String FACET_ID = "MidPointFacet";

    public static final String FACET_NAME = "MidPoint";

    public MidPointFacetType() {
        super(FACET_TYPE_ID, FACET_ID, FACET_NAME);
    }

    @Override
    public MidPointFacetConfiguration createDefaultConfiguration() {
        return new MidPointFacetConfiguration();
    }

    @Override
    public MidPointFacet createFacet(@NotNull Module module, String name,
                                     @NotNull MidPointFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new MidPointFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return true;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return MidPointIcons.ACTION_MIDPOINT;
    }
}
