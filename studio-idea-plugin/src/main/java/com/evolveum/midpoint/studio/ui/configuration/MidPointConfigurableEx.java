package com.evolveum.midpoint.studio.ui.configuration;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfigurableEx extends MidPointConfigurable {

    @Override
    public MidPointSettingsState loadModel() {
        return MidPointSettings.getInstance().getState();
    }

    @Override
    public void apply() {
        boolean modified = isModified();

        super.apply();

        if (modified) {
            MidPointSettings ms = MidPointSettings.getInstance();
            ms.setState(getModel());
        }
    }
}
