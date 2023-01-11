package com.evolveum.midpoint.studio.action;

import com.intellij.ide.ui.customization.CustomizableActionGroupProvider;
import com.intellij.ui.ExperimentalUI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExpUiActionGroupProvider extends CustomizableActionGroupProvider {

    @Override
    public void registerGroups(CustomizableActionGroupRegistrar registrar) {
        if (registrar == null) {
            return;
        }

        if (!ExperimentalUI.isNewUI()) {
            return;
        }

        registrar.addCustomizableActionGroup("MidPoint.Toolbar.Main", "MidPoint Toolbar");
    }
}
