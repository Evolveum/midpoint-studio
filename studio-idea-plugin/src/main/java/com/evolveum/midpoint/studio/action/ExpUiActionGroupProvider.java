package com.evolveum.midpoint.studio.action;

import com.intellij.ide.ui.customization.CustomizableActionGroupProvider;
import com.intellij.ui.NewUI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExpUiActionGroupProvider extends CustomizableActionGroupProvider {

    @Override
    public void registerGroups(CustomizableActionGroupRegistrar registrar) {
        if (registrar == null) {
            return;
        }

        if (!NewUI.isEnabled()) {
            return;
        }

        registrar.addCustomizableActionGroup("MidPoint.Toolbar.Main", "MidPoint Toolbar");
    }
}
