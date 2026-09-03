package com.evolveum.midpoint.studio.ui.dialog.wizard;

import javax.swing.JPanel;


public interface WizardContent {

    JPanel getPanel();

    default void beforeChangeAction() {
    }

    default void afterChangeAction() {
    }

    default boolean disableChangeStep() {
        return false;
    }
}
