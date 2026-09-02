package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.model.common.expression.script.mel.extension.MidPointCelExtensionManager;
import com.evolveum.midpoint.model.common.expression.script.mel.value.PolyStringCelValue;
import dev.cel.checker.CelStandardDeclarations;
import dev.cel.common.CelFunctionDecl;
import dev.cel.common.CelOverloadDecl;
import dev.cel.common.types.CelType;
import dev.cel.extensions.CelExtensionLibrary;
import dev.cel.parser.CelMacro;
import dev.cel.parser.CelStandardMacro;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Builds MEL function metadata by introspecting CEL function declarations from the midPoint
 * extension libraries (model-common) and the stock CEL extension libraries that
 * MidPointCelExtensionManager registers.
 */
class MelCelIntrospector {

    /**
     * Valid MEL function declaration name: identifier or dot-separated identifiers.
     */
    private static final Pattern DECL_NAME =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    record Result(
            Map<String, Map<String, MelExtensionRegistry.ExtensionFunction>> namespaced,
            Map<String, MelExtensionRegistry.ExtensionFunction> bare,
            Set<String> macroFunctions,
            Set<String> standardFunctions) {
    }

    static Result introspect() {
        Iterable<? extends CelExtensionLibrary.FeatureSet> featureSets = loadFeatureSets();

        // decl full name -> collected overloads, sorted for determinism
        Map<String, List<MelExtensionRegistry.Overload>> byDeclName = new TreeMap<>();
        Set<String> macros = new TreeSet<>();

        for (CelExtensionLibrary.FeatureSet featureSet : featureSets) {
            for (CelFunctionDecl decl : featureSet.functions()) {
                if (!DECL_NAME.matcher(decl.name()).matches()) {
                    continue; // operator overloads such as "_+_"
                }
                List<MelExtensionRegistry.Overload> overloads =
                        byDeclName.computeIfAbsent(decl.name(), k -> new ArrayList<>());
                for (CelOverloadDecl overload : decl.overloads()) {
                    overloads.add(toOverload(overload));
                }
            }
            for (CelMacro macro : featureSet.macros()) {
                macros.add(macro.getFunction());
            }
        }
        for (CelStandardMacro macro : CelStandardMacro.STANDARD_MACROS) {
            macros.add(macro.getFunction());
        }

        Map<String, Map<String, MelExtensionRegistry.ExtensionFunction>> namespaced = new TreeMap<>();
        Map<String, MelExtensionRegistry.ExtensionFunction> bare = new TreeMap<>();
        for (var entry : byDeclName.entrySet()) {
            String declName = entry.getKey();
            int dot = declName.lastIndexOf('.');
            if (dot >= 0) {
                String namespace = declName.substring(0, dot);
                String name = declName.substring(dot + 1);
                namespaced.computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                        .put(name, new MelExtensionRegistry.ExtensionFunction(
                                namespace, name, List.copyOf(entry.getValue())));
            } else {
                bare.put(declName, new MelExtensionRegistry.ExtensionFunction(
                        null, declName, List.copyOf(entry.getValue())));
            }
        }

        Set<String> standard = CelStandardDeclarations.getAllFunctionNames().stream()
                .filter(name -> DECL_NAME.matcher(name).matches())
                .collect(Collectors.toCollection(TreeSet::new));

        return new Result(
                Collections.unmodifiableMap(namespaced),
                Collections.unmodifiableMap(bare),
                Collections.unmodifiableSet(macros),
                Collections.unmodifiableSet(standard));
    }

    private static MelExtensionRegistry.Overload toOverload(CelOverloadDecl overload) {
        List<String> parameterTypes = overload.parameterTypes().stream()
                .map(MelCelIntrospector::displayType)
                .toList();
        return new MelExtensionRegistry.Overload(
                overload.isInstanceFunction(),
                parameterTypes,
                displayType(overload.resultType()),
                overload.doc());
    }

    /**
     * Maps CEL type names to the short names used in MEL documentation and completion.
     * NullableType.name() delegates to its target type, so nullability is transparent.
     * midPoint types (the *CelValue classes in model-common) are named by Java class and
     * handled by the default rule; MelExtensionRegistryTest verifies each registered type
     * renders as a short name, and the golden surface test pins the concrete spellings.
     */
    static String displayType(CelType type) {
        String name = type.name();
        // The one spelling the default rule cannot derive: MEL documentation uses
        // all-lowercase "polystring", the class name would yield "polyString".
        if (PolyStringCelValue.POLYSTRING_PACKAGE_NAME.equals(name)) {
            return "polystring";
        }
        return switch (name) {
            case "google.protobuf.Timestamp" -> "timestamp";
            case "google.protobuf.Duration" -> "duration";
            case "google.protobuf.Any" -> "any";
            default -> {
                if (!name.contains(".")) {
                    yield name; // string, bool, int, list, map, dyn, ...
                }
                String simple = name.substring(name.lastIndexOf('.') + 1);
                if (simple.endsWith("Type") && simple.length() > "Type".length()) {
                    simple = simple.substring(0, simple.length() - "Type".length());
                }
                yield Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
            }
        };
    }

    private static Iterable<? extends CelExtensionLibrary.FeatureSet> loadFeatureSets() {
        MidPointCelExtensionManager manager = new MidPointCelExtensionManager(
                null, null, null, null, null);

        // Insertion order kept so that overload order is stable across runs when two
        // libraries declare the same function name (HashSet order varies per JVM).
        Set<CelExtensionLibrary.FeatureSet> libraries = new LinkedHashSet<>();
        libraries.addAll(filterLibraries(manager.getCompilerLibraries(null)));
        libraries.addAll(filterLibraries(manager.getRuntimeLibraries(null)));

        return libraries;
    }

    private static List<CelExtensionLibrary.FeatureSet> filterLibraries(Iterable<?> libs) {
        return StreamSupport.stream(libs.spliterator(), false)
                .map(l -> l instanceof CelExtensionLibrary.FeatureSet fs ? fs : null)
                .filter(Objects::nonNull)
                .toList();
    }
}
