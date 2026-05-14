package com.evolveum.midpoint.studio.lang.mel.impl;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MelSemanticAnalyzer extends com.evolveum.midpoint.studio.lang.mel.antlr.MELBaseVisitor<Void> {

    private static final MelExtensionRegistry REGISTRY = new MelExtensionRegistry();

    // MEL variables — namespace identifiers (format, log, ldap, secret) are derived from the
    // registry so that adding a new namespace there automatically allows its bare use as a receiver.
    private static final Set<String> KNOWN_IDENTIFIERS;

    static {
        var ids = new java.util.HashSet<>(Set.of("focus", "projection", "now"));
        ids.addAll(REGISTRY.namespaces());
        KNOWN_IDENTIFIERS = Collections.unmodifiableSet(ids);
    }

    private static final Set<String> KNOWN_MEMBER_FUNCTIONS = Set.of(
            "contains", "startsWith", "endsWith", "matches", "lower", "upper",
            "filter", "map", "exists", "all", "find",
            "isEffectivelyEnabled", "connectorConfiguration", "orig", "norm", "decrypt"
    );

    private static final Set<String> KNOWN_GLOBAL_FUNCTIONS = Set.of(
            // standard CEL
            "size", "matches", "type",
            // MEL built-ins
            "isEmpty", "isNull", "isPresent", "default", "timestamp", "duration", "qname"
    );

    private final List<ValidationError> diagnostics = new ArrayList<>();

    List<ValidationError> analyze(ParseTree tree) {
        visit(tree);
        return Collections.unmodifiableList(diagnostics);
    }

    @Override
    public Void visitIdent(com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.IdentContext ctx) {
        String name = ctx.id.getText();
        if (!KNOWN_IDENTIFIERS.contains(name)) {
            diagnostics.add(warning(ctx.id, "Unknown identifier '" + name + "'"));
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitGlobalCall(com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.GlobalCallContext ctx) {
        String name = ctx.id.getText();
        if (!KNOWN_GLOBAL_FUNCTIONS.contains(name)) {
            diagnostics.add(warning(ctx.id, "Unknown function '" + name + "'"));
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitMemberCall(com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.MemberCallContext ctx) {
        String name = ctx.id.getText();
        String namespace = getNamespaceReceiver(ctx);
        if (namespace != null) {
            // Namespace call: validate function name against the registry
            if (!REGISTRY.isValidNamespaceCall(namespace, name)) {
                diagnostics.add(warning(ctx.id,
                        "Unknown function '" + name + "' on extension '" + namespace + "'"));
            }
        } else {
            // Non-namespace member call: accept known member functions and dual-mode extension functions
            if (!KNOWN_MEMBER_FUNCTIONS.contains(name) && !REGISTRY.isValidMemberCall(name)) {
                diagnostics.add(warning(ctx.id, "Unknown member function '" + name + "'"));
            }
        }
        return visitChildren(ctx);
    }

    /**
     * If the direct receiver of this member call is a known extension namespace identifier,
     * returns that namespace name; otherwise returns null.
     */
    private String getNamespaceReceiver(com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.MemberCallContext ctx) {
        if (ctx.member() instanceof com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.PrimaryExprContext primaryExpr
                && primaryExpr.primary() instanceof com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.IdentContext identCtx) {
            String id = identCtx.id.getText();
            return REGISTRY.isNamespace(id) ? id : null;
        }
        return null;
    }

    private ValidationError warning(Token token, String message) {
        return new ValidationError(token, message);
    }

    public static class ValidationError {
        public final Token token;
        public final String message;

        public ValidationError(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }
}
