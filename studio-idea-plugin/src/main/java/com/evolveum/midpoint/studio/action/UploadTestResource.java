package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.UploadOptions;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResource extends UploadBaseAction {

    @Override
    protected UploadOptions buildAddOptions() {
        return super.buildAddOptions().testConnection(true);
    }
}
