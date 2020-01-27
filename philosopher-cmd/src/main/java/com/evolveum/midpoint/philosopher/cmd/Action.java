package com.evolveum.midpoint.philosopher.cmd;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Action<T> {

    void init(T options) throws Exception;

    void execute() throws Exception;
}
