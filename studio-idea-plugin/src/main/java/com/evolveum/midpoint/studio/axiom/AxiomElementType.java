package com.evolveum.midpoint.studio.axiom;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomElementType extends IElementType {

    public AxiomElementType(@NotNull @NonNls String debugName) {
        super(debugName, AxiomLanguage.INSTANCE);
    }
}
