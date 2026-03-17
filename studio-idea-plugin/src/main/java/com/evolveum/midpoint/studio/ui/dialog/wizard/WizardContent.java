package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.intellij.ui.components.JBPanel;

public interface WizardContent {

    void onStateChanged();
    JBPanel<?> getPanel();
}
