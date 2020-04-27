package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.impl.UploadOptions;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadRaw extends UploadBaseAction {

    @Override
    protected UploadOptions buildAddOptions() {
        return super.buildAddOptions().raw(true);
    }
}
