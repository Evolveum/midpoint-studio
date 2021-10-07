package com.evolveum.midpoint.studio.cmd;

import com.beust.jcommander.JCommander;
import com.evolveum.midpoint.studio.cmd.opts.BaseOptions;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import com.evolveum.midpoint.studio.cmd.util.Log;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;

import java.nio.charset.Charset;

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

    public void init() {

    }

    public void destroy() {

    }

    public void setLog(Log log) {
        this.log = log;
    }

    public BaseOptions getBaseOptions() {
        return StudioUtil.getOptions(jc, BaseOptions.class);
    }

    public EnvironmentOptions getEnvironmentOptions() {
        return StudioUtil.getOptions(jc, EnvironmentOptions.class);
    }

    public Charset getCharset() {
        return Charset.forName(getBaseOptions().getCharset());
    }
}
