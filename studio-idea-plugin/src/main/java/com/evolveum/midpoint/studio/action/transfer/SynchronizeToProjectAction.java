package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.ui.diff.SynchronizationDirection;
import com.intellij.icons.AllIcons;

public class SynchronizeToProjectAction extends SynchronizeObjectsAction {

    public static final String ACTION_NAME = "Synchronize to project";

    public SynchronizeToProjectAction() {
        super(ACTION_NAME, AllIcons.Actions.Download, SynchronizationDirection.TO_PROJECT);
    }
}
