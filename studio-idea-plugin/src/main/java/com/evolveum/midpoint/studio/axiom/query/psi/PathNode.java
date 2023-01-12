package com.evolveum.midpoint.studio.axiom.query.psi;

import com.evolveum.midpoint.studio.axiom.PsiElementFactory;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PathNode extends ASTWrapperPsiElement {

    public PathNode(@NotNull ASTNode node) {
        super(node);
    }

    public static class Factory implements PsiElementFactory {

        public static Factory INSTANCE = new Factory();

        @Override
        public PsiElement createElement(ASTNode node) {
            return new PathNode(node);
        }
    }
}
