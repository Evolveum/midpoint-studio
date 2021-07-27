package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

import com.evolveum.midpoint.studio.cmd.executor.configuration.Task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ModifyTask extends Task {

    private ModifyConfiguration modify;

    public ModifyConfiguration getModify() {
        return modify;
    }

    public void setModify(ModifyConfiguration modify) {
        this.modify = modify;
    }
}
