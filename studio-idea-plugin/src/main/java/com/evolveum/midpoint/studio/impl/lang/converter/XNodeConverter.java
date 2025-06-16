package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.prism.xnode.XNodeFactory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface XNodeConverter {

     XNodeFactory xNodeFactory = new XNodeFactoryImpl();

    @Nullable
    XNode convertFromPsi(@NotNull PsiElement element);
}
