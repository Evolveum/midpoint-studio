package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELBaseVisitor;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.GlobalCallContext;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.IdentContext;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.MemberCallContext;
import com.evolveum.midpoint.studio.lang.mel.antlr.MELParser.PrimaryExprContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MelExtensionValidator extends MELBaseVisitor<Void> {

    static final MelExtensionRegistry REGISTRY = new MelExtensionRegistry();

    // MEL variables — namespace identifiers (format, log, ldap, secret) are derived from the
    // registry so that adding a new namespace there automatically allows its bare use as a receiver.
    static final Set<String> KNOWN_IDENTIFIERS;

    static {
        var ids = new java.util.HashSet<>(Set.of("focus", "projection", "now"));
        ids.addAll(REGISTRY.namespaces());
        KNOWN_IDENTIFIERS = Collections.unmodifiableSet(ids);
    }

    static final Set<String> KNOWN_MEMBER_FUNCTIONS = Set.of(
            "contains", "startsWith", "endsWith", "matches", "lower", "upper",
            "filter", "map", "exists", "all", "find",
            "isEffectivelyEnabled", "connectorConfiguration", "orig", "norm", "decrypt"
    );

    static final Set<String> KNOWN_GLOBAL_FUNCTIONS = Set.of(
            // standard CEL
            "size", "matches", "type",
            // MEL built-ins
            "isEmpty", "isNull", "isPresent", "default", "timestamp", "duration", "qname"
    );

    private final List<ValidationMessage> messages = new ArrayList<>();

    public List<ValidationMessage> analyze(ParseTree tree) {
        visit(tree);

        return Collections.unmodifiableList(messages);
    }

    @Override
    public Void visitIdent(IdentContext ctx) {
        String name = ctx.id.getText();
        if (!KNOWN_IDENTIFIERS.contains(name)) {
            messages.add(warning(ctx.id, "Unknown identifier '" + name + "'"));
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitGlobalCall(GlobalCallContext ctx) {
        String name = ctx.id.getText();
        if (!KNOWN_GLOBAL_FUNCTIONS.contains(name)) {
            messages.add(error(ctx.id, "Unknown function '" + name + "'"));
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitMemberCall(MemberCallContext ctx) {
        String name = ctx.id.getText();
        String namespace = getNamespaceReceiver(ctx);
        if (namespace != null) {
            // Namespace call: validate function name against the registry
            if (!REGISTRY.isValidNamespaceCall(namespace, name)) {
                messages.add(error(ctx.id,
                        "Unknown function '" + name + "' on extension '" + namespace + "'"));
            }
        } else {
            // Non-namespace member call: accept known member functions and dual-mode extension functions
            if (!KNOWN_MEMBER_FUNCTIONS.contains(name) && !REGISTRY.isValidMemberCall(name)) {
                messages.add(error(ctx.id, "Unknown member function '" + name + "'"));
            }
        }
        return visitChildren(ctx);
    }

    /**
     * If the direct receiver of this member call is a known extension namespace identifier,
     * returns that namespace name; otherwise returns null.
     */
    private String getNamespaceReceiver(MemberCallContext ctx) {
        if (ctx.member() instanceof PrimaryExprContext primaryExpr
                && primaryExpr.primary() instanceof IdentContext identCtx) {
            String id = identCtx.id.getText();
            return REGISTRY.isNamespace(id) ? id : null;
        }
        return null;
    }

    private ValidationMessage warning(Token token, String message) {
        return new ValidationMessage(token, ValidationSeverity.WARNING, message);
    }

    private ValidationMessage error(Token token, String message) {
        return new ValidationMessage(token, ValidationSeverity.ERROR, message);
    }
}
