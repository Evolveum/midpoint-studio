package com.evolveum.midpoint.studio.lang.mel.impl;

import java.util.*;

public class MelExtensionRegistry {

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

    // Map: namespace -> (functionName -> ExtensionFunction)
    private static final Map<String, Map<String, ExtensionFunction>> EXTENSIONS;

    // Secondary index: function name -> ExtensionFunction, for dual-mode functions only (non-empty receiverTypes)
    private static final Set<String> DUAL_MODE_FUNCTIONS;

    static {
        EXTENSIONS = Map.of(
                "format", formatFunctions(),
                "log", logFunctions(),
                "ldap", ldapFunctions(),
                "secret", secretFunctions()
        );
        DUAL_MODE_FUNCTIONS = EXTENSIONS.values().stream()
                .flatMap(m -> m.values().stream())
                .filter(f -> !f.receiverTypes().isEmpty())
                .map(ExtensionFunction::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
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


    private static Map<String, ExtensionFunction> formatFunctions() {
        var fns = new ArrayList<ExtensionFunction>();

        // namespace-only (no member-call form)
        fns.add(fn("concatName",
                Set.of(),
                List.of(new Parameter("components", "list")),
                "string", false));

        // dual-mode: timestamp receiver
        fns.add(fn("strftime",
                Set.of("timestamp"),
                List.of(new Parameter("value", "timestamp"), new Parameter("format", "string")),
                "string", false));
        fns.add(fn("formatDateTime",
                Set.of("timestamp"),
                List.of(new Parameter("value", "timestamp"), new Parameter("format", "string")),
                "string", false));

        // dual-mode: string receiver
        fns.add(fn("strptime",
                Set.of("string"),
                List.of(new Parameter("value", "string"), new Parameter("format", "string")),
                "timestamp", false));
        fns.add(fn("parseDateTime",
                Set.of("string"),
                List.of(new Parameter("value", "string"), new Parameter("format", "string")),
                "timestamp", false));
        fns.add(fn("parseGivenName",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));
        fns.add(fn("parseFamilyName",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));
        fns.add(fn("parseAdditionalName",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));
        fns.add(fn("parseNickName",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));
        fns.add(fn("parseHonorificPrefix",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));
        fns.add(fn("parseHonorificSuffix",
                Set.of("string"),
                List.of(new Parameter("value", "string")),
                "string", false));

        return toMap(fns);
    }

    private static Map<String, ExtensionFunction> logFunctions() {
        // All log functions share the same parameter shape and are variadic
        var params = List.of(new Parameter("format", "string"), new Parameter("value", "any"));
        return toMap(List.of(
                fn("info", Set.of(), params, "any", true),
                fn("error", Set.of(), params, "any", true),
                fn("warn", Set.of(), params, "any", true),
                fn("debug", Set.of(), params, "any", true),
                fn("trace", Set.of(), params, "any", true)
        ));
    }

    private static Map<String, ExtensionFunction> ldapFunctions() {
        return toMap(List.of(
                fn("composeDn",
                        Set.of(),
                        List.of(new Parameter("components", "list")),
                        "string", false),
                fn("composeDnWithSuffix",
                        Set.of(),
                        List.of(new Parameter("components", "list")),
                        "string", false),
                fn("hashPassword",
                        Set.of(),
                        List.of(new Parameter("password", "any"), new Parameter("algorithm", "string")),
                        "string", false),
                fn("determineSingleAttributeValue",
                        Set.of(),
                        List.of(
                                new Parameter("dn", "string"),
                                new Parameter("attributeName", "string"),
                                new Parameter("values", "list")
                        ),
                        "string", false)
        ));
    }

    private static Map<String, ExtensionFunction> secretFunctions() {
        var params = List.of(new Parameter("provider", "string"), new Parameter("key", "string"));
        return toMap(List.of(
                fn("resolveBinary", Set.of(), params, "bytes", false),
                fn("resolveString", Set.of(), params, "string", false),
                fn("resolveProtectedString", Set.of(), params, "protectedString", false)
        ));
    }

    private static ExtensionFunction fn(String name, Set<String> receiverTypes,
                                        List<Parameter> parameters,
                                        String returnType, boolean variadic) {
        return new ExtensionFunction(name, receiverTypes, parameters, returnType, variadic);
    }

    private static Map<String, ExtensionFunction> toMap(List<ExtensionFunction> fns) {
        var map = new LinkedHashMap<String, ExtensionFunction>();
        for (var f : fns) map.put(f.name(), f);
        return Collections.unmodifiableMap(map);
    }
}
