package com.evolveum.midpoint.studio.lang.mel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MelExtensionRegistry {

    private static final String DEFINITIONS_RESOURCE = "/mel-extensions.json";

    record Parameter(String name, String type) {
    }

    record ExtensionFunction(
            String name,
            Set<String> receiverTypes,
            List<Parameter> parameters,
            String returnType,
            boolean variadic
    ) {
    }

    // JSON DTOs, mirroring mel-extensions.json structure
    private record Definitions(Map<String, Namespace> namespaces) {
    }

    private record Namespace(List<FunctionDef> functions) {
    }

    private record FunctionDef(
            String name,
            List<String> receiverTypes,
            List<Parameter> parameters,
            String returnType,
            boolean variadic
    ) {
    }

    // Map: namespace -> (functionName -> ExtensionFunction)
    private static final Map<String, Map<String, ExtensionFunction>> EXTENSIONS;

    // Secondary index: function name -> ExtensionFunction, for dual-mode functions only (non-empty receiverTypes)
    private static final Set<String> DUAL_MODE_FUNCTIONS;

    static {
        EXTENSIONS = loadExtensions();
        DUAL_MODE_FUNCTIONS = EXTENSIONS.values().stream()
                .flatMap(m -> m.values().stream())
                .filter(f -> !f.receiverTypes().isEmpty())
                .map(ExtensionFunction::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static Map<String, Map<String, ExtensionFunction>> loadExtensions() {
        var mapper = new ObjectMapper();

        try (InputStream is = MelExtensionRegistry.class.getResourceAsStream(DEFINITIONS_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("MEL extension definitions not found on classpath: " + DEFINITIONS_RESOURCE);
            }

            var definitions = mapper.readValue(is, Definitions.class);

            var result = new LinkedHashMap<String, Map<String, ExtensionFunction>>();
            for (var entry : definitions.namespaces().entrySet()) {
                var fns = entry.getValue().functions().stream()
                        .map(f -> new ExtensionFunction(
                                f.name(),
                                Set.copyOf(f.receiverTypes()),
                                f.parameters(),
                                f.returnType(),
                                f.variadic()))
                        .toList();
                result.put(entry.getKey(), toMap(fns));
            }
            return Collections.unmodifiableMap(result);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load MEL extension definitions from " + DEFINITIONS_RESOURCE, e);
        }
    }

    // -------------------------------------------------------------------------
    // Query API (used by MelSemanticAnalyzer today)
    // -------------------------------------------------------------------------

    /**
     * Returns true if {@code function} is a valid call on the given extension namespace.
     */
    boolean isValidNamespaceCall(String namespace, String function) {
        var fns = EXTENSIONS.get(namespace);
        return fns != null && fns.containsKey(function);
    }

    /**
     * Returns true if {@code function} is valid as a dual-mode member call on any receiver.
     * Specifically: returns true if and only if the function is registered under any namespace
     * with a non-empty receiverTypes set. Namespace-only functions (empty receiverTypes) return
     * false. A set containing "*" is also non-empty and therefore returns true (future wildcard).
     */
    boolean isValidMemberCall(String function) {
        return DUAL_MODE_FUNCTIONS.contains(function);
    }

    /**
     * Returns the set of all known extension namespace identifiers.
     */
    Set<String> namespaces() {
        return EXTENSIONS.keySet();
    }

    /**
     * Returns true if {@code identifier} is a known extension namespace.
     */
    boolean isNamespace(String identifier) {
        return EXTENSIONS.containsKey(identifier);
    }

    Collection<ExtensionFunction> functionsForNamespace(String namespace) {
        var fns = EXTENSIONS.get(namespace);
        return fns != null ? fns.values() : List.of();
    }

    private static Map<String, ExtensionFunction> toMap(List<ExtensionFunction> fns) {
        var map = new LinkedHashMap<String, ExtensionFunction>();
        for (var f : fns) map.put(f.name(), f);
        return Collections.unmodifiableMap(map);
    }
}
