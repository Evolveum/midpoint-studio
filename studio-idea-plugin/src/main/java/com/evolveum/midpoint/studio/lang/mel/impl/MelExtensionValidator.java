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

    // Expression variables provided by the MEL evaluator. Extension namespaces are known
    // identifiers too, see knownIdentifiers(); they come from the registry so that a new
    // namespace in midPoint automatically allows its bare use as a receiver.
    static final Set<String> VARIABLES = Set.of("focus", "projection", "now", "nil");

    // Callable names that are real in MEL but not introspectable from CEL function
    // declarations (e.g. polystring virtual fields used with call syntax). Coverage of the
    // pre-introspection hardcoded lists is asserted by MelExtensionRegistryTest; keep this
    // as small as that test allows.
    static final Set<String> EXTRA_MEMBER_FUNCTIONS = Set.of("orig", "norm");

    static final Set<String> EXTRA_GLOBAL_FUNCTIONS = Set.of();

    private final List<ValidationMessage> messages = new ArrayList<>();

    private final MelExtensionRegistry registry;

    public MelExtensionValidator() {
        this(MelExtensionRegistry.get());
    }

    MelExtensionValidator(MelExtensionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Bare identifiers accepted without a declaration: evaluator variables and extension
     * namespaces of the given registry.
     */
    static Set<String> knownIdentifiers(MelExtensionRegistry registry) {
        var ids = new java.util.LinkedHashSet<>(VARIABLES);
        ids.addAll(registry.namespaces());
        return Collections.unmodifiableSet(ids);
    }

    public List<ValidationMessage> analyze(ParseTree tree) {
        visit(tree);

        return Collections.unmodifiableList(messages);
    }

    @Override
    public Void visitIdent(IdentContext ctx) {
        String name = ctx.id.getText();
        if (!VARIABLES.contains(name) && !registry.isNamespace(name)) {
            messages.add(warning(ctx.id, "Unknown identifier '" + name + "'"));
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitGlobalCall(GlobalCallContext ctx) {
        String name = ctx.id.getText();
        if (!isKnownGlobal(name)) {
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
            if (!registry.isValidNamespaceCall(namespace, name)) {
                messages.add(error(ctx.id,
                        "Unknown function '" + name + "' on extension '" + namespace + "'"));
            }
        } else {
            if (!isKnownMember(name)) {
                messages.add(error(ctx.id, "Unknown member function '" + name + "'"));
            }
        }
        return visitChildren(ctx);
    }

    private boolean isKnownGlobal(String name) {
        return registry.isValidGlobalCall(name)
                || registry.isStandardFunction(name)
                || registry.isMacro(name)
                || EXTRA_GLOBAL_FUNCTIONS.contains(name);
    }

    private boolean isKnownMember(String name) {
        // Standard CEL function names are accepted for member calls too: the public CEL API
        // does not expose member/global per standard overload, and a false accept only costs
        // a missing diagnostic, while a false reject flags valid code.
        return registry.isValidMemberCall(name)
                || registry.isStandardFunction(name)
                || registry.isMacro(name)
                || EXTRA_MEMBER_FUNCTIONS.contains(name);
    }

    /**
     * If the direct receiver of this member call is a known extension namespace identifier,
     * returns that namespace name; otherwise returns null.
     */
    private String getNamespaceReceiver(MemberCallContext ctx) {
        if (ctx.member() instanceof PrimaryExprContext primaryExpr
                && primaryExpr.primary() instanceof IdentContext identCtx) {
            String id = identCtx.id.getText();
            return registry.isNamespace(id) ? id : null;
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
