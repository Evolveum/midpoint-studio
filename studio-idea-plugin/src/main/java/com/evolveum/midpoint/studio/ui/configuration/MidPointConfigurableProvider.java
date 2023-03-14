package com.evolveum.midpoint.studio.ui.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfigurableProvider extends ConfigurableProvider {

    @Override
    public boolean canCreateConfigurable() {
        // todo implement
        return super.canCreateConfigurable();
    }

    @Override
    public @Nullable Configurable createConfigurable() {
        return new MidPointConfigurableEx();
    }
}
