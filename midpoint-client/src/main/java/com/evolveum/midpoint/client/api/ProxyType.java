package com.evolveum.midpoint.client.api;

import org.apache.cxf.transports.http.configuration.ProxyServerType;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum ProxyType {

    HTTP(ProxyServerType.HTTP),

    SOCKS(ProxyServerType.SOCKS);

    private ProxyServerType type;

    ProxyType(ProxyServerType type) {
        this.type = type;
    }

    public ProxyServerType getType() {
        return type;
    }
}
