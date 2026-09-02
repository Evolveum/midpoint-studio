package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.openapi.diagnostic.Logger;

import java.util.*;

/**
 * Registry of MEL functions available in expressions, backed by introspection of CEL
 * function declarations from midPoint extension libraries (see MelCelIntrospector).
 * Drives semantic analysis, code completion and quick documentation.
 */
public class MelExtensionRegistry {

    private static final Logger LOG = Logger.getInstance(MelExtensionRegistry.class);

    /**
     * One CEL overload. For member overloads, parameterTypes includes the receiver
     * as the first element (matching CEL's declaration model).
     */
    record Overload(boolean member, List<String> parameterTypes, String returnType, String documentation) {
    }

    /**
     * One callable MEL function. Namespace is null for bare functions (member calls
     * like value.isBlank() and globals like isBlank(value) or debugDump(x)).
     */
    record ExtensionFunction(String namespace, String name, List<Overload> overloads) {

        boolean memberCallable() {
            return overloads.stream().anyMatch(Overload::member);
        }

        boolean globalCallable() {
            return overloads.stream().anyMatch(o -> !o.member());
        }

        String returnType() {
            return overloads.get(0).returnType();
        }

        String documentation() {
            return overloads.stream()
                    .map(Overload::documentation)
                    .filter(d -> d != null && !d.isBlank())
                    .findFirst()
                    .orElse(null);
        }
    }

    // namespace -> (functionName -> function), e.g. "format" -> "strftime" -> ...
    private final Map<String, Map<String, ExtensionFunction>> namespaced;
    // bare function name -> function, e.g. "isBlank", "debugDump"
    private final Map<String, ExtensionFunction> bare;
    private final Set<String> macroFunctions;
    private final Set<String> standardFunctions;

    MelExtensionRegistry() {
        MelCelIntrospector.Result result;
        try {
            result = MelCelIntrospector.introspect();
        } catch (Throwable t) {
            // Introspection instantiates midPoint extension libraries with null services;
            // if that contract breaks upstream, degrade to an empty registry.
            LOG.warn("MEL extension introspection failed, extension functions will be unknown", t);
            result = new MelCelIntrospector.Result(Map.of(), Map.of(), Set.of(), Set.of());
        }
        this.namespaced = result.namespaced();
        this.bare = result.bare();
        this.macroFunctions = result.macroFunctions();
        this.standardFunctions = result.standardFunctions();
    }

    boolean isValidNamespaceCall(String namespace, String function) {
        var functions = namespaced.get(namespace);
        return functions != null && functions.containsKey(function);
    }

    boolean isValidMemberCall(String function) {
        var fn = bare.get(function);
        return fn != null && fn.memberCallable();
    }

    boolean isValidGlobalCall(String function) {
        var fn = bare.get(function);
        return fn != null && fn.globalCallable();
    }

    boolean isMacro(String function) {
        return macroFunctions.contains(function);
    }

    boolean isStandardFunction(String function) {
        return standardFunctions.contains(function);
    }

    Set<String> namespaces() {
        return namespaced.keySet();
    }

    boolean isNamespace(String identifier) {
        return namespaced.containsKey(identifier);
    }

    Collection<ExtensionFunction> functionsForNamespace(String namespace) {
        var functions = namespaced.get(namespace);
        return functions != null ? functions.values() : List.of();
    }

    ExtensionFunction bareFunction(String name) {
        return bare.get(name);
    }

    Collection<ExtensionFunction> memberFunctions() {
        return bare.values().stream().filter(ExtensionFunction::memberCallable).toList();
    }

    Collection<ExtensionFunction> globalFunctions() {
        return bare.values().stream().filter(ExtensionFunction::globalCallable).toList();
    }

    Set<String> standardFunctionNames() {
        return standardFunctions;
    }

    Set<String> macroNames() {
        return macroFunctions;
    }

    /**
     * All introspected functions, for the golden surface test.
     */
    Collection<ExtensionFunction> allFunctions() {
        var all = new ArrayList<>(bare.values());
        namespaced.values().forEach(m -> all.addAll(m.values()));
        return all;
    }
}
