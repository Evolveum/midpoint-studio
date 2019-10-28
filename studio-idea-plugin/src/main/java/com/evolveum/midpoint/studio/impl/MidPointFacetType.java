package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetType extends FacetType<MidPointFacet, MidPointFacetConfiguration> {

    public static final String TYPE_ID = "MidPointFacet";

    public MidPointFacetType() {
        super(MidPointFacet.ID, TYPE_ID, "MidPoint");
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
