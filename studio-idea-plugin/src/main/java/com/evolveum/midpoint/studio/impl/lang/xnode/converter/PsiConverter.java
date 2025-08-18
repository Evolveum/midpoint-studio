package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface PsiConverter {

    @Nullable
    String convert(PsiElement element, boolean deep);

}
