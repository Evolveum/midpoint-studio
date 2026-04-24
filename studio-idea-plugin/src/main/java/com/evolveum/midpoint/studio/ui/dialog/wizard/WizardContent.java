package com.evolveum.midpoint.studio.ui.dialog.wizard;

import javax.swing.JPanel;

public interface WizardContent {

    JPanel getPanel();

    default void beforeChangeAction() throws InterruptedException {
    }

    default void afterChangeAction() {
    }
}
