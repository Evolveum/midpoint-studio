package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConsolePanel extends BorderLayoutPanel {

    private MidPointConsoleView console;

    private ActionToolbar toolbar;

    public MidPointConsolePanel(MidPointConsoleView console) {
        this.console = console;

        add(console.getComponent(), BorderLayout.CENTER);
    }

    public void setToolbar(ActionToolbar toolbar) {
        this.toolbar = toolbar;

        add(toolbar.getComponent(), BorderLayout.WEST);
    }

    public MidPointConsoleView getConsole() {
        return console;
    }

    public ActionToolbar getToolbar() {
        return toolbar;
    }
}
