/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd;

import com.evolveum.midpoint.studio.cmd.action.Action;
import com.evolveum.midpoint.studio.cmd.action.RunAction;
import com.evolveum.midpoint.studio.cmd.action.UploadAction;
import com.evolveum.midpoint.studio.cmd.opts.RunOptions;
import com.evolveum.midpoint.studio.cmd.opts.UploadOptions;

/**
 * @author Viliam Repan (lazyman)
 */
public enum Command {

    RUN("run", RunOptions.class, RunAction.class),

    UPLOAD("upload", UploadOptions.class, UploadAction.class);

    private String commandName;

    private Class options;

    private Class<? extends Action> action;

    Command(String commandName, Class options, Class<? extends Action> action) {
        this.commandName = commandName;
        this.options = options;
        this.action = action;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object createOptions() {
        try {
            return options.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Action createAction(String command) {
        Command cmd = findCommand(command);
        if (cmd == null) {
            return null;
        }

        try {
            if (cmd.action == null) {
                return null;
            }

            return cmd.action.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Command findCommand(String command) {
        if (command == null) {
            return null;
        }

        for (Command cmd : values()) {
            if (command.equals(cmd.getCommandName())) {
                return cmd;
            }
        }

        return null;
    }
}
