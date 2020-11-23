package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.impl.client.DeleteOptions;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteRawAction extends DeleteAction {

    @Override
    public DeleteOptions createOptions() {
        return new DeleteOptions().raw(true);
    }
}
