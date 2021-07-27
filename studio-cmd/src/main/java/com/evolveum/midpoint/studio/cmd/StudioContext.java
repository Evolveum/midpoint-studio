package com.evolveum.midpoint.studio.cmd;

import com.beust.jcommander.JCommander;
import com.evolveum.midpoint.studio.cmd.util.Log;

/**
 * Created by Viliam Repan (lazyman).
 */
public class StudioContext {

    private JCommander jc;

    private Log log;

    public StudioContext(JCommander jc) {
        this.jc = jc;
    }

    public JCommander getJc() {
        return jc;
    }

    public void init()  {

    }

    public void destroy() {

    }

    public void setLog(Log log) {
        this.log = log;
    }
}
