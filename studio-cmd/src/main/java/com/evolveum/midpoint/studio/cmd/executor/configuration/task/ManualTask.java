package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

import com.evolveum.midpoint.studio.cmd.executor.configuration.Task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ManualTask extends Task {

    private ManualConfiguration manual;

    public ManualConfiguration getManual() {
        return manual;
    }

    public void setManual(ManualConfiguration manual) {
        this.manual = manual;
    }
}
