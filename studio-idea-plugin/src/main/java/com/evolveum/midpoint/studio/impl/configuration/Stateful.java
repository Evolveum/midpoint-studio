package com.evolveum.midpoint.studio.impl.configuration;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Stateful<T extends Serializable> {

    T getSettings();

    void setSettings(T settings);
}
