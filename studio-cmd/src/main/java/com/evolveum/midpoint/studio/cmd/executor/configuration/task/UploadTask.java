package com.evolveum.midpoint.studio.cmd.executor.configuration.task;

import com.evolveum.midpoint.studio.cmd.executor.configuration.Task;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTask extends Task {

    private Configuration upload;

    public Configuration getUpload() {
        return upload;
    }

    public void setUpload(Configuration upload) {
        this.upload = upload;
    }

    public static class Configuration implements Serializable {

        private boolean raw;

        public boolean isRaw() {
            return raw;
        }

        public void setRaw(boolean raw) {
            this.raw = raw;
        }
    }
}
