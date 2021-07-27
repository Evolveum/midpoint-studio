package com.evolveum.midpoint.studio.cmd.executor.configuration;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidpointProperties implements Serializable {

    private String plain;

    private String encrypted;

    public String getPlain() {
        return plain;
    }

    public void setPlain(String plain) {
        this.plain = plain;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }
}
