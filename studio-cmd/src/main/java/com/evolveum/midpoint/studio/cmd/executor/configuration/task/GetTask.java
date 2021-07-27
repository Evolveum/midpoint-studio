package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

import com.evolveum.midpoint.studio.cmd.executor.configuration.Task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GetTask extends Task {

    private GetConfiguration get;

    public GetConfiguration getGet() {
        return get;
    }

    public void setGet(GetConfiguration get) {
        this.get = get;
    }
}
