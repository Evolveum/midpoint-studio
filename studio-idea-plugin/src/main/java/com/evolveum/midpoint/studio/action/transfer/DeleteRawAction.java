package com.evolveum.midpoint.studio.action.transfer;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteRawAction extends DeleteAction {

    public static final String ACTION_NAME = "Delete (raw)";

    public DeleteRawAction() {
        super(ACTION_NAME);

        setRaw(true);
    }
}
