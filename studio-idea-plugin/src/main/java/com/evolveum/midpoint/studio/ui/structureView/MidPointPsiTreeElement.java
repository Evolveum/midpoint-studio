package com.evolveum.midpoint.studio.ui.structureView;

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class MidPointPsiTreeElement<T extends PsiElement> extends PsiTreeElementBase<T> {

    public MidPointPsiTreeElement(T psiElement) {
        super(psiElement);
    }
}
