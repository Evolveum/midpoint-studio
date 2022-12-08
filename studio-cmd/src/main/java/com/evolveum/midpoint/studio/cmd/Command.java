package com.evolveum.midpoint.studio.cmd;

import com.evolveum.midpoint.studio.cmd.action.GenerateAction;
import com.evolveum.midpoint.studio.cmd.option.CmdGenerateOptions;

/**
 * @author Viliam Repan (lazyman)
 */
public enum Command {

    GENERATE("generate", CmdGenerateOptions.class, GenerateAction.class);

    private final String commandName;

    private final Class options;

    private final Class<? extends Action> action;

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
            return options.newInstance();
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

            return cmd.action.newInstance();
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
