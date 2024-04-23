package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.ui.diff.SynchronizationDirection;
import com.intellij.icons.AllIcons;

public class SynchronizeToMidPointAction extends SynchronizeObjectsAction {

    public static final String ACTION_NAME = "Synchronize to MidPoint";

    public SynchronizeToMidPointAction() {
        super(ACTION_NAME, AllIcons.Actions.Upload, SynchronizationDirection.TO_MIDPOINT);
    }
}
