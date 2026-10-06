package com.evolveum.midpoint.studio.lang.mel.impl;

import java.util.List;

/**
 * One callable MEL function. Namespace is null for bare functions (member calls
 * like value.isBlank() and globals like isBlank(value) or debugDump(x)).
 */
record MelExtensionFunction(String namespace, String name, List<MelOverload> overloads) {

    boolean memberCallable() {
        return overloads.stream().anyMatch(MelOverload::member);
    }

    boolean globalCallable() {
        return overloads.stream().anyMatch(o -> !o.member());
    }

    String returnType() {
        return overloads.get(0).returnType();
    }

    String documentation() {
        return overloads.stream()
                .map(MelOverload::documentation)
                .filter(d -> d != null && !d.isBlank())
                .findFirst()
                .orElse(null);
    }
}
