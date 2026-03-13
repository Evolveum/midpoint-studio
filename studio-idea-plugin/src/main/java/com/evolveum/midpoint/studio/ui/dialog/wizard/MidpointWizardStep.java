package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.evolveum.midpoint.studio.ui.dialog.wizard.navigation.NavigationItem;

import javax.swing.*;

public class MidpointWizardStep {

    private NavigationItem navigationItem;
    private JPanel contentPanel;

    public MidpointWizardStep(NavigationItem navigationItem, JPanel contentPanel) {
        this.navigationItem = navigationItem;
        this.contentPanel = contentPanel;
    }

    public NavigationItem getNavigationItem() {
        return navigationItem;
    }

    public void setNavigationItem(NavigationItem navigationItem) {
        this.navigationItem = navigationItem;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setContentPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }
}
