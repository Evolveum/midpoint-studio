package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.prism.PrismContext;

import java.lang.reflect.Proxy;

/**
 * Prism context stand-ins for client tests that never touch prism, so no schema loading is needed.
 */
final class TestPrismContexts {

    private TestPrismContexts() {
    }

    /**
     * Prism context that throws on any call, only its identity matters.
     */
    static PrismContext inert() {
        return (PrismContext) Proxy.newProxyInstance(
                PrismContext.class.getClassLoader(),
                new Class<?>[]{PrismContext.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException("Inert prism context: " + method.getName());
                });
    }
}
