package com.evolveum.midpoint.studio.cmd.executor.configuration;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidpointUser implements Serializable {

    private String name;

    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
