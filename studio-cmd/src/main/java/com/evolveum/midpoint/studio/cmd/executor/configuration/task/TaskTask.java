package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

import com.evolveum.midpoint.studio.cmd.executor.configuration.Task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskTask extends Task {

    private TaskConfiguration task;

    public TaskConfiguration getTask() {
        return task;
    }

    public void setTask(TaskConfiguration task) {
        this.task = task;
    }
}
