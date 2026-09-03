package com.evolveum.midpoint.studio.lang.mel.impl;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Verifies that MEL function metadata introspected from CEL declarations
 * (model-common extension libraries) is working correctly.
 */
public class MelExtensionRegistryTest {

    private static final MelExtensionRegistry REGISTRY = new MelExtensionRegistry();

    @Test
    public void allLibrariesIntrospected() {
        // Rough lower bound; a significant drop means a library silently stopped loading
        assertTrue("suspiciously few functions introspected: " + REGISTRY.allFunctions().size(),
                REGISTRY.allFunctions().size() > 150);
    }

    @Test
    public void namespacesPresent() {
        Set<String> namespaces = REGISTRY.namespaces();
        for (String expected : List.of("format", "ldap", "log", "secret", "midpoint", "math")) {
            assertTrue("missing namespace: " + expected, namespaces.contains(expected));
        }
    }

    @Test
    public void namespaceCallsValid() {
        assertTrue(REGISTRY.isValidNamespaceCall("format", "strftime"));
        assertTrue(REGISTRY.isValidNamespaceCall("ldap", "composeDn"));
        assertTrue(REGISTRY.isValidNamespaceCall("log", "info"));
        assertTrue(REGISTRY.isValidNamespaceCall("secret", "resolveString"));
        assertTrue(REGISTRY.isValidNamespaceCall("midpoint", "getObject"));
        assertFalse(REGISTRY.isValidNamespaceCall("format", "noSuchFunction"));
    }

    @Test
    public void bareFunctionsValid() {
        // string member and global forms of isBlank (from the mel library)
        assertTrue(REGISTRY.isValidMemberCall("isBlank"));
        assertTrue(REGISTRY.isValidGlobalCall("isBlank"));
        // global-only function from the log library
        assertTrue(REGISTRY.isValidGlobalCall("debugDump"));
        // member functions from the object library
        assertTrue(REGISTRY.isValidMemberCall("isEffectivelyEnabled"));
        assertTrue(REGISTRY.isValidMemberCall("connectorConfiguration"));
        assertFalse(REGISTRY.isValidMemberCall("noSuchFunction"));
    }

    @Test
    public void standardFunctionsAndMacros() {
        assertTrue(REGISTRY.isStandardFunction("size"));
        assertTrue(REGISTRY.isStandardFunction("matches"));
        // operator names like _+_ must be filtered out
        assertFalse(REGISTRY.isStandardFunction("_+_"));
        assertTrue(REGISTRY.isMacro("exists"));
        assertTrue(REGISTRY.isMacro("map"));
        assertTrue(REGISTRY.isMacro("has"));
    }

    @Test
    public void typeNamesAreMelFriendly() {
        var strftime = REGISTRY.functionsForNamespace("format").stream()
                .filter(f -> f.name().equals("strftime"))
                .findFirst().orElseThrow();
        var overload = strftime.overloads().get(0);
        assertEquals(List.of("timestamp", "string"), overload.parameterTypes());
        assertEquals("string", overload.returnType());
        assertNotNull(overload.documentation());
        assertFalse(overload.documentation().isBlank());
    }

    /**
     * Every CEL type registered by midPoint's CelTypeMapper must render as a short display
     * name, never as a raw fully qualified class name. Uses CelTypeMapper as the enumeration
     * source.
     */
    @Test
    public void midPointTypesRenderAsShortNames() {
        var mapper = new com.evolveum.midpoint.model.common.expression.script.mel.CelTypeMapper(null);
        for (var type : mapper.types()) {
            String display = MelCelIntrospector.displayType(type);
            assertTrue("bad display name '" + display + "' for CEL type " + type.name(),
                    display.matches("[a-z][A-Za-z0-9]*"));
        }
    }

    /**
     * Golden test of the whole introspected MEL functions. When a midpoint upgrade changes
     * the functions this fails; review the diff, then copy the actual file from
     * build/mel-extensions-golden-actual.txt over src/test/resources/mel-extensions-golden.txt.
     */
    @Test
    public void goldenTest() throws Exception {
        String actual = REGISTRY.allFunctions().stream()
                .flatMap(fn -> fn.overloads().stream().map(o -> String.format("%s%s|%s|(%s)|%s",
                        fn.namespace() == null ? "" : fn.namespace() + ".",
                        fn.name(),
                        o.member() ? "member" : "global",
                        String.join(",", o.parameterTypes()),
                        o.returnType())))
                .sorted()
                .collect(java.util.stream.Collectors.joining("\n", "", "\n"));

        java.nio.file.Path resource = java.nio.file.Path.of("src/test/resources/mel-extensions-golden.txt");
        java.nio.file.Path actualOut = java.nio.file.Path.of("build/mel-extensions-golden-actual.txt");
        java.nio.file.Files.createDirectories(actualOut.getParent());
        java.nio.file.Files.writeString(actualOut, actual);

        assertTrue("golden file missing; generated functions written to " + actualOut.toAbsolutePath(),
                java.nio.file.Files.exists(resource));
        String expected = java.nio.file.Files.readString(resource);
        assertEquals("MEL functions changed (midpoint upgrade?); actual written to " + actualOut.toAbsolutePath(),
                expected, actual);
    }
}
