package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Listener {

    <T> void onEvent(Event<T> evt);
}
