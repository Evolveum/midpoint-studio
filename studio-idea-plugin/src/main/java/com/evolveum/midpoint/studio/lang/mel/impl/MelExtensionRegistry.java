package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

/**
 * Registry of MEL functions available in expressions, backed by introspection of CEL
 * function declarations from midPoint extension libraries (see MelCelIntrospector).
 * Drives semantic analysis, code completion and quick documentation.
 */
public class MelExtensionRegistry {

    private static final Logger LOG = Logger.getInstance(MelExtensionRegistry.class);

    // namespace -> (functionName -> function), e.g. "format" -> "strftime" -> ...
    private final Map<String, Map<String, MelExtensionFunction>> namespaced;
    // bare function name -> function, e.g. "isBlank", "debugDump"
    private final Map<String, MelExtensionFunction> bare;
    private final Set<String> macroFunctions;
    private final Set<String> standardFunctions;

    /**
     * Registry backed by live introspection. Introspection instantiates midPoint extension
     * libraries with null services; if that contract breaks upstream, degrades to an empty
     * registry and logs the cause so it can be found in idea.log.
     */
    static MelExtensionRegistry create() {
        try {
            return new MelExtensionRegistry(MelCelIntrospector.introspect());
        } catch (Throwable t) {
            LOG.warn("MEL extension introspection failed, registry is empty and all MEL extension"
                    + " functions will be reported as unknown: " + t, t);
            return new MelExtensionRegistry(
                    new MelCelIntrospector.Result(Map.of(), Map.of(), Set.of(), Set.of()));
        }
    }

    @VisibleForTesting
    MelExtensionRegistry(MelCelIntrospector.Result result) {
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

    Collection<MelExtensionFunction> functionsForNamespace(String namespace) {
        var functions = namespaced.get(namespace);
        return functions != null ? functions.values() : List.of();
    }

    MelExtensionFunction bareFunction(String name) {
        return bare.get(name);
    }

    Collection<MelExtensionFunction> memberFunctions() {
        return bare.values().stream().filter(MelExtensionFunction::memberCallable).toList();
    }

    Collection<MelExtensionFunction> globalFunctions() {
        return bare.values().stream().filter(MelExtensionFunction::globalCallable).toList();
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
    Collection<MelExtensionFunction> allFunctions() {
        var all = new ArrayList<>(bare.values());
        namespaced.values().forEach(m -> all.addAll(m.values()));
        return all;
    }
}
