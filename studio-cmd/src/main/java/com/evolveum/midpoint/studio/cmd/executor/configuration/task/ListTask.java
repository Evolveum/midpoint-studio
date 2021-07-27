package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ListTask extends TaskTask {

    private ListConfiguration list;

    public ListConfiguration getList() {
        return list;
    }

    public void setList(ListConfiguration list) {
        this.list = list;
    }
}
