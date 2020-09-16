package com.evolveum.midpoint.studio.impl.client;

import java.net.Proxy;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum ProxyType {

    HTTP(Proxy.Type.HTTP),

    SOCKS(Proxy.Type.SOCKS);

    private Proxy.Type type;

    ProxyType(Proxy.Type type) {
        this.type = type;
    }

    public Proxy.Type getType() {
        return type;
    }
}
