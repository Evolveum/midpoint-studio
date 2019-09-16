package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.evolveum.midpoint.studio.impl.UploadOptions;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResource extends UploadBaseAction {

    @Override
    protected UploadOptions buildAddOptions() {
        return super.buildAddOptions().testConnection(true);
    }

    // todo move to
    protected void executeAction(AnActionEvent evt, PrismObject obj) {


//        printToConsole(evt.getProject(), "Resource '" + obj.getName() + "' connection test: " + status);
    }
}
