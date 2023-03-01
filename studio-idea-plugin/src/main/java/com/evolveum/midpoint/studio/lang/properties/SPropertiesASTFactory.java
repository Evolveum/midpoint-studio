package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.psi.SPropertiesPath;
import com.evolveum.midpoint.studio.lang.properties.psi.SPropertiesPathItem;
import com.evolveum.midpoint.studio.lang.properties.psi.SPropertiesProperty;
import com.evolveum.midpoint.studio.lang.properties.psi.SPropertiesRoot;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesASTFactory extends ASTFactory {

    @Override
    public @Nullable CompositeElement createComposite(@NotNull IElementType type) {
        if (type instanceof IFileElementType) {
            return new FileElement(type, null);
        }

        if (type == SPropertiesTokenTypes.getRuleElementType(RULE_root)) {
            return new SPropertiesRoot();
        }

        if (type == SPropertiesTokenTypes.getRuleElementType(RULE_property)) {
            return new SPropertiesProperty();
        }

        if (type == SPropertiesTokenTypes.getRuleElementType(RULE_path)) {
            return new SPropertiesPath();
        }

        if (type == SPropertiesTokenTypes.getRuleElementType(RULE_pathItem)) {
            return new SPropertiesPathItem();
        }

        return new CompositeElement(type);
    }

    @Override
    public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        return new LeafPsiElement(type, text);
    }

    public static PsiElement createInternalParseTreeNode(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }
}
