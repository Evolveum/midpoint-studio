package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.intellij.ui.components.JBPanel;


public interface WizardContent {

    JBPanel<?> getPanel();

    default void beforeChangeAction() {
    }

    default void afterChangeAction() {
    }

    default boolean disableChangeStep() {
        return false;
    }
}
